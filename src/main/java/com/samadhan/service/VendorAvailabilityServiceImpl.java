package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.samadhan.dto.GeoPoint;
import com.samadhan.dto.MatchingRequestsResponse;
import com.samadhan.dto.RouteRequest;
import com.samadhan.dto.RouteResponse;
import com.samadhan.dto.RouteWaypoint;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorAvailability;
import com.samadhan.entity.VendorAvailabilityMatch;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.exception.ResourceNotFoundException;
import com.samadhan.exception.SubscriptionSuspendedException;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VendorAvailabilityMatchRepository;
import com.samadhan.repository.VendorAvailabilityRepository;
import com.samadhan.request.VendorAvailabilityRequest;
import com.samadhan.util.GeoUtils;

@Service
public class VendorAvailabilityServiceImpl implements VendorAvailabilityService {

	private static final Logger log = LoggerFactory.getLogger(VendorAvailabilityServiceImpl.class);

	// Radius for treating a pickup as "at" one of the posting's declared endpoints.
	private static final double ENDPOINT_RADIUS_KM = 40.0;
	// Corridor buffer for treating a pickup as "along the way" on the from->to driving route.
	private static final double ROUTE_CORRIDOR_KM = 15.0;
	// Minimum spacing between kept route-polyline vertices for the corridor distance check below —
	// Google's polylines run a vertex every few meters, and checking every match candidate against
	// every one of those is the dominant cost of this scoring. Thinned to this spacing via
	// GeoUtils.simplifyPolyline; worst-case added distance error is roughly half this value (up to
	// the full value on a sharply curving stretch) — at 4km that's ~13-27% of ROUTE_CORRIDOR_KM's
	// 15km tolerance, still well short of it.
	private static final double ROUTE_SIMPLIFY_SPACING_KM = 4.0;
	// Bounding-box padding around the posting's endpoints, applied before precise distance checks.
	private static final double BOUNDING_BOX_BUFFER_DEG = 0.5;
	// A candidate's own pickup->drop distance must be at least this fraction of the posting's
	// total from->to distance to count as a match — otherwise a tiny local hop near the posting's
	// starting point (e.g. an 8km same-city errand) shows up as "matches your posting" against a
	// 500km cross-city trip, which is technically true by proximity but not a meaningful match.
	private static final double MIN_RIDE_DISTANCE_RATIO = 0.20;
	// A whole-vehicle request must need at least this fraction of the posting's vehicle capacity
	// to count as a match — otherwise a scooter-sized booking (a few kg) shows up as "matches
	// your posting" against a 20ft truck (10 tonnes), which passes the plain "big enough" check
	// but is such a size mismatch it's not a meaningful match either.
	private static final double MIN_VEHICLE_CAPACITY_RATIO = 0.30;

	@Autowired
	VendorAvailabilityRepository vendorAvailabilityRepository;

	@Autowired
	TransferVendorRepository transferVendorRepository;

	@Autowired
	TransferRequestRepository transferRequestRepository;

	@Autowired
	VendorAvailabilityMatchRepository vendorAvailabilityMatchRepository;

	@Autowired
	RouteService routeService;

	// Self-injected proxy reference — @Lazy defers resolution so Spring doesn't choke on this
	// bean depending on itself during construction. Needed because postAvailability/
	// updateAvailability/cancelAvailability/bootstrapMatchesIfEmpty call recomputeAndPersistForVendor
	// on `this` (a plain, non-proxied call within the same class instance), and Spring's
	// @Transactional support is proxy-based — it only takes effect when a method is invoked
	// *through* the proxy from outside the class, not via such a same-class self-invocation.
	// Going through `self` instead routes the call through the real proxy so @Transactional on
	// recomputeAndPersistForVendor actually applies.
	@Autowired
	@Lazy
	private VendorAvailabilityService self;

	// NOT @Transactional — this method calls routeService.getRoute(...) below, an external HTTP
	// call to Google's Routes API. Wrapping the whole method in a transaction would hold a DB
	// connection checked out for that call's entire duration, including any slow response or
	// outage on Google's side — a classic "don't hold a DB transaction across network I/O"
	// mistake. The DB-only work that actually needs a transaction (recomputeAndPersistForVendor's
	// delete-then-insert) gets its own short transaction instead, via the self-injected proxy
	// call in safeRecomputeForVendor — see that method and recomputeAndPersistForVendor's
	// @Transactional for why. Same reasoning applies to updateAvailability below.
	@Override
	public VendorAvailability postAvailability(VendorAvailabilityRequest request) {
		if (request.vendorId == null) {
			throw new IllegalArgumentException("vendorId is required");
		}
		if (request.toLocation == null || request.toLocation.trim().isEmpty()) {
			throw new IllegalArgumentException("toLocation is required");
		}
		if (request.expectedDate == null) {
			throw new IllegalArgumentException("expectedDate is required");
		}

		TransferVendor vendor = transferVendorRepository.findById(request.vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + request.vendorId));

		// Same subscription gate TransferRequestServiceImpl.requestRideTransfer applies before
		// letting a vendor take on new rides — an expired/suspended vendor shouldn't be able to
		// post availability either, since that's just another way of soliciting new rides.
		if (vendor.getVendorStatus().name().equals("SUSPENDED")) {
			throw new SubscriptionSuspendedException("Your subscription is suspended. Please contact support or renew your subscription.");
		} else if (vendor.getVendorStatus().name().equals("SUBSCRIPTION_PENDING")) {
			throw new SubscriptionSuspendedException("Your free subscription Period is over. Buy your subscription.");
		}

		VendorAvailability availability = new VendorAvailability();
		availability.setTransferVendor(vendor);
		availability.setFromLocation(request.fromLocation);
		availability.setFromLatitude(request.fromLatitude);
		availability.setFromLongitude(request.fromLongitude);
		availability.setToLocation(request.toLocation);
		availability.setToLatitude(request.toLatitude);
		availability.setToLongitude(request.toLongitude);
		availability.setExpectedDate(request.expectedDate);
		availability.setVehicleType(request.vehicleType);
		availability.setVehicleCategory(request.vehicleCategory);
		availability.setVehicleNumber(request.vehicleNumber);
		availability.setReturnTrip(request.returnTrip != null && request.returnTrip);
		availability.setWaypoints(request.waypoints);
		availability.setActive(true);
		availability.setCreatedAt(LocalDateTime.now());

		VendorAvailability saved = vendorAvailabilityRepository.save(availability);

		Double fromLat = GeoUtils.parseCoord(request.fromLatitude);
		Double fromLng = GeoUtils.parseCoord(request.fromLongitude);
		Double toLat = GeoUtils.parseCoord(request.toLatitude);
		Double toLng = GeoUtils.parseCoord(request.toLongitude);

		// Waypoints with valid coordinates only — an un-geocoded one (e.g. typed but never
		// selected from suggestions on the frontend) is silently dropped from the route
		// computation rather than failing the whole posting over one bad stop.
		List<GeoPoint> routeWaypoints = new ArrayList<>();
		if (request.waypoints != null) {
			for (RouteWaypoint wp : request.waypoints) {
				Double wLat = GeoUtils.parseCoord(wp.getLatitude());
				Double wLng = GeoUtils.parseCoord(wp.getLongitude());
				if (wLat != null && wLng != null) {
					routeWaypoints.add(new GeoPoint(wLat, wLng));
				}
			}
		}

		if (fromLat != null && fromLng != null && toLat != null && toLng != null) {
			try {
				RouteResponse route = routeService.getRoute(new RouteRequest(
						new GeoPoint(fromLat, fromLng), new GeoPoint(toLat, toLng), "DRIVE", false, routeWaypoints));
				saved.setRoutePolyline(route.getPolyline());
				saved = vendorAvailabilityRepository.save(saved);
				log.info("Computed route polyline for vendor availability {} ({} chars)",
						saved.getId(), route.getPolyline() == null ? 0 : route.getPolyline().length());
			} catch (Exception ex) {
				// Best-effort: without a cached route, matching still works via endpoint-radius checks.
				log.warn("Could not compute route for vendor availability {}: {}", saved.getId(), ex.getMessage());
			}
		} else {
			// Logged at info (not warn) — this is the frontend not having resolved coordinates for
			// from/to yet (e.g. typed but no suggestion selected), not a failure on this side, but
			// still worth a trace since it's the other reason (besides the try/catch above) a
			// posting can end up with no route_polyline.
			log.info("Skipping route computation for vendor availability {} — fromLat/fromLng/toLat/toLng "
					+ "not all resolved (from=[{},{}], to=[{},{}])", saved.getId(), fromLat, fromLng, toLat, toLng);
		}

		// A new posting changes this vendor's own match set immediately — recomputed and persisted
		// right away rather than waiting for the next trigger.
		safeRecomputeForVendor(request.vendorId);

		return saved;
	}

	// NOT @Transactional — see postAvailability's comment above; this method also calls
	// routeService.getRoute(...).
	@Override
	public VendorAvailability updateAvailability(Long vendorId, Long availabilityId, VendorAvailabilityRequest request) {
		if (request.toLocation == null || request.toLocation.trim().isEmpty()) {
			throw new IllegalArgumentException("toLocation is required");
		}
		if (request.expectedDate == null) {
			throw new IllegalArgumentException("expectedDate is required");
		}

		VendorAvailability availability = vendorAvailabilityRepository.findById(availabilityId)
				.orElseThrow(() -> new ResourceNotFoundException("Availability not found: " + availabilityId));

		if (availability.getTransferVendor() == null || !vendorId.equals(availability.getTransferVendor().getId())) {
			throw new AccessDeniedException("You are not authorized to edit this availability posting");
		}

		availability.setFromLocation(request.fromLocation);
		availability.setFromLatitude(request.fromLatitude);
		availability.setFromLongitude(request.fromLongitude);
		availability.setToLocation(request.toLocation);
		availability.setToLatitude(request.toLatitude);
		availability.setToLongitude(request.toLongitude);
		availability.setExpectedDate(request.expectedDate);
		availability.setVehicleType(request.vehicleType);
		availability.setVehicleCategory(request.vehicleCategory);
		availability.setVehicleNumber(request.vehicleNumber);
		availability.setReturnTrip(request.returnTrip != null && request.returnTrip);
		availability.setWaypoints(request.waypoints);

		Double fromLat = GeoUtils.parseCoord(request.fromLatitude);
		Double fromLng = GeoUtils.parseCoord(request.fromLongitude);
		Double toLat = GeoUtils.parseCoord(request.toLatitude);
		Double toLng = GeoUtils.parseCoord(request.toLongitude);

		List<GeoPoint> routeWaypoints = new ArrayList<>();
		if (request.waypoints != null) {
			for (RouteWaypoint wp : request.waypoints) {
				Double wLat = GeoUtils.parseCoord(wp.getLatitude());
				Double wLng = GeoUtils.parseCoord(wp.getLongitude());
				if (wLat != null && wLng != null) {
					routeWaypoints.add(new GeoPoint(wLat, wLng));
				}
			}
		}

		// Route/waypoints may have changed, so the cached polyline is re-derived rather than kept
		// stale from the pre-edit route — same best-effort behavior as postAvailability if the
		// route call fails.
		if (fromLat != null && fromLng != null && toLat != null && toLng != null) {
			try {
				RouteResponse route = routeService.getRoute(new RouteRequest(
						new GeoPoint(fromLat, fromLng), new GeoPoint(toLat, toLng), "DRIVE", false, routeWaypoints));
				availability.setRoutePolyline(route.getPolyline());
				log.info("Computed route polyline for vendor availability {} ({} chars)",
						availability.getId(), route.getPolyline() == null ? 0 : route.getPolyline().length());
			} catch (Exception ex) {
				log.warn("Could not compute route for vendor availability {}: {}", availability.getId(), ex.getMessage());
			}
		} else {
			availability.setRoutePolyline(null);
			log.info("Skipping route computation for vendor availability {} — fromLat/fromLng/toLat/toLng "
					+ "not all resolved (from=[{},{}], to=[{},{}])", availability.getId(), fromLat, fromLng, toLat, toLng);
		}

		VendorAvailability saved = vendorAvailabilityRepository.save(availability);
		// Same reasoning as postAvailability — the edited route/vehicle/dates should reflect in
		// this vendor's matches right away.
		safeRecomputeForVendor(vendorId);
		return saved;
	}

	@Override
	public List<VendorAvailability> getActiveForVendor(Long vendorId) {
		return vendorAvailabilityRepository.findByTransferVendorIdAndActiveTrueOrderByExpectedDateAsc(vendorId);
	}

	// NOT @Transactional — no external call here (unlike postAvailability/updateAvailability),
	// but kept consistent with them: recomputeAndPersistForVendor gets its own short transaction
	// via the self-injected proxy call in safeRecomputeForVendor, same as every other caller.
	@Override
	public void cancelAvailability(Long vendorId, Long availabilityId) {
		VendorAvailability availability = vendorAvailabilityRepository.findById(availabilityId)
				.orElseThrow(() -> new ResourceNotFoundException("Availability not found: " + availabilityId));

		if (availability.getTransferVendor() == null || !vendorId.equals(availability.getTransferVendor().getId())) {
			throw new AccessDeniedException("You are not authorized to cancel this availability posting");
		}

		availability.setActive(false);
		vendorAvailabilityRepository.save(availability);
		// A cancelled posting should stop contributing matches immediately.
		safeRecomputeForVendor(vendorId);
	}

	// Wraps recomputeAndPersistForVendor so a failure here (e.g. a transient DB hiccup) can never
	// turn into an exception thrown out of postAvailability/updateAvailability/cancelAvailability —
	// the posting itself is already saved by the time this runs; worst case the match table is
	// stale until the next successful recompute, not a failed posting-edit request. Calls through
	// `self` (not a plain `this` call) so recomputeAndPersistForVendor's @Transactional actually
	// takes effect — see the field comment on `self` above.
	private void safeRecomputeForVendor(Long vendorId) {
		try {
			self.recomputeAndPersistForVendor(vendorId);
		} catch (Exception e) {
			log.warn("Failed to recompute posting matches for vendor {}: {}", vendorId, e.getMessage(), e);
		}
	}

	// For each active posting, finds PENDING/unassigned requests either along the posting's own
	// from->to route (always), or — only when the vendor explicitly opted into a return trip on
	// that posting — along the reverse to->from route as well, so an empty backhaul leg isn't
	// silently assumed just because a pickup happens to be near fromLocation. Each match is
	// tagged with why it matched and a 0-100 percent score; a request matching more than once
	// keeps its RETURN_TRIP tag over POSTING_ROUTE (a vendor-confirmed return trip is a stronger
	// signal than incidental route proximity), or whichever match scores higher within the same
	// tag (see mergeOutcome).
	@Override
	public MatchingRequestsResponse getRequestsMatchingAvailability(Long vendorId, int page, int size, LocalDate pickupDate) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		Pageable pageable = PageRequest.of(safePage, safeSize);

		Page<VendorAvailabilityMatch> matchPage;
		if (pickupDate != null) {
			boolean isToday = pickupDate.isEqual(LocalDate.now());
			matchPage = vendorAvailabilityMatchRepository.findByVendorIdAndPickupDateFiltered(
					vendorId, pickupDate, isToday, pageable);
		} else {
			matchPage = vendorAvailabilityMatchRepository.findByVendorIdOrderByRequestCreatedDateDesc(vendorId, pageable);
		}

		List<VendorAvailabilityMatch> matchRows = matchPage.getContent();
		List<Long> requestIds = matchRows.stream()
				.map(VendorAvailabilityMatch::getTransferRequestId)
				.collect(Collectors.toList());
		Map<Long, TransferRequestDetails> requestsById = transferRequestRepository.findAllById(requestIds).stream()
				.collect(Collectors.toMap(TransferRequestDetails::getId, r -> r));

		List<TransferRequestDetails> matches = new ArrayList<>();
		for (VendorAvailabilityMatch row : matchRows) {
			TransferRequestDetails request = requestsById.get(row.getTransferRequestId());
			if (request == null) {
				// The request was removed/purged after this match row was written but before this
				// read — skip rather than fail the whole page over one stale row.
				continue;
			}
			request.setMatchType(row.getMatchType());
			request.setMatchDistanceKm(row.getMatchDistanceKm());
			request.setMatchScorePercent(row.getMatchScorePercent());
			request.setVehicleMatchPercent(row.getVehicleMatchPercent());
			matches.add(request);
		}

		MatchingRequestsResponse response = new MatchingRequestsResponse();
		response.setMatches(matches);
		response.setTotalElements(matchPage.getTotalElements());
		response.setTotalPages(matchPage.getTotalPages());
		response.setPage(safePage);
		response.setSize(safeSize);
		return response;
	}

	// Recomputes this ONE vendor's full match set against every currently pending/unassigned
	// request, and replaces its rows in vendor_availability_match. This is the same computation
	// the old computeMatches did, just persisted instead of returned/cached in memory.
	//
	// @Transactional: needed because deleteByVendorId (below) is a Spring Data derived delete
	// query — it removes rows via the persistence context (EntityManager.remove()) rather than an
	// immediate DML statement, which requires an active transaction. Only takes effect when
	// called via `self` (see that field's comment) rather than a plain same-class call, since
	// Spring's @Transactional support is proxy-based.
	@Override
	@Transactional
	public void recomputeAndPersistForVendor(Long vendorId) {
		List<VendorAvailability> postings =
				vendorAvailabilityRepository.findByTransferVendorIdAndActiveTrueOrderByExpectedDateAsc(vendorId);

		Map<Long, MatchOutcome> bestByRequestId = new LinkedHashMap<>();
		Map<Long, TransferRequestDetails> candidateById = new LinkedHashMap<>();

		if (!postings.isEmpty()) {
			LocalDate latestExpectedDate = postings.stream()
					.map(VendorAvailability::getExpectedDate)
					.filter(java.util.Objects::nonNull)
					.max(Comparator.naturalOrder())
					.orElse(LocalDate.now());
			List<TransferRequestDetails> candidatePool =
					transferRequestRepository.findPendingUnassignedUpTo(latestExpectedDate);

			for (VendorAvailability posting : postings) {
				PostingContext ctx = buildPostingContext(posting);
				if (ctx == null) {
					continue;
				}
				for (TransferRequestDetails candidate : candidatePool) {
					if (!withinDateAndBox(candidate, ctx)) {
						continue;
					}
					candidateById.put(candidate.getId(), candidate);
					for (MatchOutcome outcome : scoreCandidateAgainstPosting(posting, candidate, ctx)) {
						mergeOutcome(bestByRequestId, candidate.getId(), outcome);
					}
				}
			}
		}

		LocalDateTime now = LocalDateTime.now();
		List<VendorAvailabilityMatch> rows = new ArrayList<>();
		for (Map.Entry<Long, MatchOutcome> entry : bestByRequestId.entrySet()) {
			TransferRequestDetails candidate = candidateById.get(entry.getKey());
			rows.add(toMatchRow(vendorId, candidate, entry.getValue(), now));
		}

		// Delete-then-insert rather than a finer-grained diff — this table is a precomputed
		// convenience index (not a source of truth), and a vendor's own posting edits are a
		// low-frequency, explicitly-triggered action, so a full replace here is simple and cheap
		// enough not to need incremental upsert logic.
		//
		// The flush() is required, not optional: deleteByVendorId is a derived Spring Data delete
		// query, which removes matching rows via the persistence context (entityManager.remove())
		// rather than an immediate DML statement — Hibernate can defer actually executing those
		// deletes until the next flush. saveAll's inserts, however, use GenerationType.IDENTITY,
		// which executes immediately (it needs the DB-generated id back right away). Without an
		// explicit flush in between, an insert for the same (vendor_id, transfer_request_id) pair
		// as a row that was just "deleted" (but not yet physically removed) hits the unique
		// constraint — this is what caused "Duplicate entry" errors on every recompute, not just
		// under concurrent access.
		vendorAvailabilityMatchRepository.deleteByVendorId(vendorId);
		vendorAvailabilityMatchRepository.flush();
		vendorAvailabilityMatchRepository.saveAll(rows);
	}

	// Recomputes match rows for this ONE request against every vendor's active postings —
	// called when a request newly enters (or re-enters) the pending/unassigned pool.
	@Override
	public void recomputeForRequest(Long transferRequestId) {
		TransferRequestDetails candidate = transferRequestRepository.findById(transferRequestId).orElse(null);
		if (candidate == null) {
			vendorAvailabilityMatchRepository.deleteByTransferRequestId(transferRequestId);
			return;
		}
		// Only pending/unassigned requests belong in the match pool — if this request has already
		// moved on by the time this runs (e.g. two triggers raced), there's nothing to compute.
		if (candidate.getTransferStatus() != rideStatusEnum.PENDING || candidate.getVehicleId() != null) {
			vendorAvailabilityMatchRepository.deleteByTransferRequestId(transferRequestId);
			return;
		}
		// A request whose pickup date has already passed shouldn't be suggested as a match for a
		// future posting just because it's still sitting in PENDING/unassigned — the date check
		// below only bounds how far in the FUTURE a candidate can be relative to the posting
		// (isAfter(expectedDate)), with no lower bound at all, so a months-old stale request could
		// otherwise keep matching indefinitely. Exempts instant/immediate bookings: their
		// pickupDate is stamped as the day the request was made (see
		// TransferRequestServiceImpl#requestRideTransfer), not a literal scheduled date — the
		// actual intent is "as soon as possible," which stays valid and urgent even if it's now
		// showing a date in the past, same "instantBooking overrides the literal date" treatment
		// already used by the pickupDate filter in getRequestsMatchingAvailability.
		boolean isInstantBooking = Boolean.TRUE.equals(candidate.getInstantBooking());
		if (!isInstantBooking && candidate.getPickupDate() != null
				&& candidate.getPickupDate().isBefore(LocalDate.now())) {
			vendorAvailabilityMatchRepository.deleteByTransferRequestId(transferRequestId);
			return;
		}

		List<VendorAvailability> postings = vendorAvailabilityRepository.findByActiveTrueOrderByExpectedDateAsc();
		Map<Long, MatchOutcome> bestByVendorId = new LinkedHashMap<>();

		for (VendorAvailability posting : postings) {
			if (posting.getTransferVendor() == null) {
				continue;
			}
			if (posting.getExpectedDate() != null && candidate.getPickupDate() != null
					&& candidate.getPickupDate().isAfter(posting.getExpectedDate())) {
				continue;
			}
			PostingContext ctx = buildPostingContext(posting);
			if (ctx == null) {
				continue;
			}
			// Single-candidate path — the bounding-box pre-filter in withinDateAndBox exists to
			// cheaply reject candidates when scanning many of them per posting (see
			// recomputeAndPersistForVendor); with only one candidate to test, go straight to the
			// precise scoring check instead.
			for (MatchOutcome outcome : scoreCandidateAgainstPosting(posting, candidate, ctx)) {
				mergeOutcome(bestByVendorId, posting.getTransferVendor().getId(), outcome);
			}
		}

		LocalDateTime now = LocalDateTime.now();
		List<VendorAvailabilityMatch> rows = new ArrayList<>();
		for (Map.Entry<Long, MatchOutcome> entry : bestByVendorId.entrySet()) {
			rows.add(toMatchRow(entry.getKey(), candidate, entry.getValue(), now));
		}

		// flush() required before the insert — see recomputeAndPersistForVendor's comment above.
		vendorAvailabilityMatchRepository.deleteByTransferRequestId(transferRequestId);
		vendorAvailabilityMatchRepository.flush();
		vendorAvailabilityMatchRepository.saveAll(rows);
	}

	@Override
	public void invalidateForRequest(Long transferRequestId) {
		vendorAvailabilityMatchRepository.deleteByTransferRequestId(transferRequestId);
	}

	// NOT @Transactional — each vendor's safeRecomputeForVendor call already gets its own short
	// transaction (see recomputeAndPersistForVendor's @Transactional), which is preferable here
	// to one giant transaction spanning every vendor with an active posting at startup.
	@Override
	public void bootstrapMatchesIfEmpty() {
		if (vendorAvailabilityMatchRepository.count() > 0) {
			return;
		}
		List<Long> vendorIds = vendorAvailabilityRepository.findDistinctVendorIdByActiveTrue();
		for (Long vendorId : vendorIds) {
			safeRecomputeForVendor(vendorId);
		}
	}

	// Per-posting values that don't depend on the candidate being scored — computed once per
	// posting and reused across every candidate checked against it (or, in recomputeForRequest's
	// direction, once per posting for the single candidate being checked).
	private static final class PostingContext {
		Double fromLat;
		Double fromLng;
		double toLat;
		double toLng;
		List<double[]> waypointCoords;
		List<double[]> routePoints;
		VendorPickupVehicleEnum postingVehicle;
		Double postingDistanceKm;
		LocalDate expectedDate;
		double boxMinLat;
		double boxMaxLat;
		double boxMinLng;
		double boxMaxLng;
	}

	private static final class MatchOutcome {
		final String matchType;
		final double distanceKm;
		final int scorePercent;
		final Integer vehicleMatchPercent;

		MatchOutcome(String matchType, double distanceKm, int scorePercent, Integer vehicleMatchPercent) {
			this.matchType = matchType;
			this.distanceKm = distanceKm;
			this.scorePercent = scorePercent;
			this.vehicleMatchPercent = vehicleMatchPercent;
		}
	}

	private PostingContext buildPostingContext(VendorAvailability posting) {
		Double toLat = GeoUtils.parseCoord(posting.getToLatitude());
		Double toLng = GeoUtils.parseCoord(posting.getToLongitude());
		if (toLat == null || toLng == null) {
			return null;
		}
		Double fromLat = GeoUtils.parseCoord(posting.getFromLatitude());
		Double fromLng = GeoUtils.parseCoord(posting.getFromLongitude());

		double minLat = (fromLat != null) ? Math.min(fromLat, toLat) : toLat;
		double maxLat = (fromLat != null) ? Math.max(fromLat, toLat) : toLat;
		double minLng = (fromLng != null) ? Math.min(fromLng, toLng) : toLng;
		double maxLng = (fromLng != null) ? Math.max(fromLng, toLng) : toLng;

		// Vendor-specified intermediate stops — widen the bounding box to include them (a detour
		// via a waypoint can sit well outside the straight-line from->to box) and keep their
		// coordinates for the direct waypoint-proximity check, independent of whether
		// routePolyline successfully bent through them.
		List<double[]> waypointCoords = new ArrayList<>();
		for (RouteWaypoint wp : posting.getWaypoints()) {
			Double wLat = GeoUtils.parseCoord(wp.getLatitude());
			Double wLng = GeoUtils.parseCoord(wp.getLongitude());
			if (wLat == null || wLng == null) {
				continue;
			}
			waypointCoords.add(new double[] { wLat, wLng });
			minLat = Math.min(minLat, wLat);
			maxLat = Math.max(maxLat, wLat);
			minLng = Math.min(minLng, wLng);
			maxLng = Math.max(maxLng, wLng);
		}

		PostingContext ctx = new PostingContext();
		ctx.fromLat = fromLat;
		ctx.fromLng = fromLng;
		ctx.toLat = toLat;
		ctx.toLng = toLng;
		ctx.waypointCoords = waypointCoords;
		ctx.boxMinLat = minLat - BOUNDING_BOX_BUFFER_DEG;
		ctx.boxMaxLat = maxLat + BOUNDING_BOX_BUFFER_DEG;
		ctx.boxMinLng = minLng - BOUNDING_BOX_BUFFER_DEG;
		ctx.boxMaxLng = maxLng + BOUNDING_BOX_BUFFER_DEG;
		ctx.expectedDate = posting.getExpectedDate();

		// Direction-agnostic — a straight-line distance to the nearest point on this road
		// corridor is the same whether the vendor is driving it from->to or to->from, so the
		// same decoded polyline serves both legs. Simplified once per posting so the per-candidate
		// distance check further down isn't paying for Google's full vertex density.
		ctx.routePoints = GeoUtils.simplifyPolyline(
				GeoUtils.decodePolyline(posting.getRoutePolyline()), ROUTE_SIMPLIFY_SPACING_KM);

		// The specific vehicle this posting is for (not vehicleCategory — that's the coarser
		// SMALL_VEHICLE/OPEN_BODY_TRUCK/etc. grouping; vehicleType holds the exact
		// VendorPickupVehicleEnum display name, e.g. "Tata Ace"). Null when the vendor picked
		// "Any vehicle from my fleet" or the string doesn't parse — in either case the size
		// check below is skipped rather than guessed at.
		VendorPickupVehicleEnum postingVehicle = null;
		if (posting.getVehicleType() != null && !posting.getVehicleType().isBlank()) {
			try {
				postingVehicle = VendorPickupVehicleEnum.fromValue(posting.getVehicleType());
			} catch (IllegalArgumentException ex) {
				postingVehicle = null;
			}
		}
		ctx.postingVehicle = postingVehicle;

		// Straight-line, not the driving-route distance — always available regardless of whether
		// routePolyline was successfully computed, and only used as the yardstick for
		// MIN_RIDE_DISTANCE_RATIO, not for precise distance checks.
		ctx.postingDistanceKm = (fromLat != null && fromLng != null)
				? GeoUtils.haversineKm(fromLat, fromLng, toLat, toLng) : null;

		return ctx;
	}

	private boolean withinDateAndBox(TransferRequestDetails candidate, PostingContext ctx) {
		// Excludes stale requests whose pickup date has already passed — see the equivalent check
		// in recomputeForRequest for why this lower bound is needed alongside the upper one below,
		// and why instant/immediate bookings are exempted from it.
		boolean isInstantBooking = Boolean.TRUE.equals(candidate.getInstantBooking());
		if (!isInstantBooking && candidate.getPickupDate() != null
				&& candidate.getPickupDate().isBefore(LocalDate.now())) {
			return false;
		}
		if (ctx.expectedDate != null && candidate.getPickupDate() != null
				&& candidate.getPickupDate().isAfter(ctx.expectedDate)) {
			return false;
		}
		Double cLat = GeoUtils.parseCoord(candidate.getSourceLatitude());
		Double cLng = GeoUtils.parseCoord(candidate.getSourceLongitude());
		if (cLat == null || cLng == null) {
			return false;
		}
		return cLat >= ctx.boxMinLat && cLat <= ctx.boxMaxLat && cLng >= ctx.boxMinLng && cLng <= ctx.boxMaxLng;
	}

	// The actual per-(posting, candidate) scoring — extracted verbatim from the original inline
	// per-candidate loop so both directions (one vendor against many candidates, or one candidate
	// against many vendors' postings) share identical scoring. Returns 0, 1, or 2 outcomes: a
	// forward/route match and/or (only for return-trip postings) a return-leg match.
	private List<MatchOutcome> scoreCandidateAgainstPosting(
			VendorAvailability posting, TransferRequestDetails candidate, PostingContext ctx) {

		Double srcLat = GeoUtils.parseCoord(candidate.getSourceLatitude());
		Double srcLng = GeoUtils.parseCoord(candidate.getSourceLongitude());
		if (srcLat == null || srcLng == null) {
			return List.of();
		}

		// ---- Forward leg (always evaluated): pickup near the posting's own starting point
		// (about to drive right past/through there), near its destination, near a vendor-
		// specified waypoint, or anywhere along the from->to corridor. nearOrigin/nearWaypoint
		// matter independently of onRoute — the route polyline is a best-effort cache computed at
		// posting time and can be missing/failed, in which case onRoute is always false and these
		// would otherwise be the only remaining way to catch pickups at those points.
		Double distToSource = (ctx.fromLat != null && ctx.fromLng != null)
				? GeoUtils.haversineKm(srcLat, srcLng, ctx.fromLat, ctx.fromLng) : null;
		double distToDest = GeoUtils.haversineKm(srcLat, srcLng, ctx.toLat, ctx.toLng);
		Double distToRoute = ctx.routePoints.isEmpty() ? null
				: GeoUtils.minDistanceToPolylineKm(srcLat, srcLng, ctx.routePoints);
		Double distToWaypoint = null;
		for (double[] wp : ctx.waypointCoords) {
			double d = GeoUtils.haversineKm(srcLat, srcLng, wp[0], wp[1]);
			if (distToWaypoint == null || d < distToWaypoint) {
				distToWaypoint = d;
			}
		}
		boolean nearOrigin = distToSource != null && distToSource <= ENDPOINT_RADIUS_KM;
		boolean nearDest = distToDest <= ENDPOINT_RADIUS_KM;
		boolean onRoute = distToRoute != null && distToRoute <= ROUTE_CORRIDOR_KM;
		boolean nearWaypoint = distToWaypoint != null && distToWaypoint <= ENDPOINT_RADIUS_KM;

		// Candidate's own pickup->drop location, parsed once here and reused below by both the
		// forward-direction gate and the return-trip check.
		Double candidateDestLat = GeoUtils.parseCoord(candidate.getDestinationLatitude());
		Double candidateDestLng = GeoUtils.parseCoord(candidate.getDestinationLongitude());

		// A forward match requires the candidate to actually be heading the same general way as
		// the posting, not just picking up somewhere near it — otherwise a pickup right at the
		// posting's destination (nearDest) would match even if the job then heads off in a
		// completely different direction (e.g. a "Noida -> Agra" job matching a "Lucknow -> Noida"
		// posting just because it starts in Noida, when it's actually the vendor's OWN drop-off
		// point and Agra isn't on the way anywhere near Lucknow). Measured as the dot product of
		// the posting's from->to vector and the candidate's own pickup->drop vector: positive means
		// broadly the same direction, negative means the candidate runs backward against the
		// posting's direction. Skipped (no restriction) when the posting has no fromLocation (no
		// direction to compare against) or the candidate's destination isn't known — same
		// permissive-fallback convention as the other checks in this method. Not applied to the
		// return-trip check below, which is intentionally the reverse direction and already has
		// its own explicit "drops back near the origin" requirement.
		boolean directionOk = true;
		if (ctx.fromLat != null && ctx.fromLng != null && candidateDestLat != null && candidateDestLng != null) {
			double postingVectorLat = ctx.toLat - ctx.fromLat;
			double postingVectorLng = ctx.toLng - ctx.fromLng;
			double candidateVectorLat = candidateDestLat - srcLat;
			double candidateVectorLng = candidateDestLng - srcLng;
			double dot = postingVectorLat * candidateVectorLat + postingVectorLng * candidateVectorLng;
			directionOk = dot >= 0;
		}

		// Whole-vehicle jobs (BOOKVEHICLE, HOMESHIFTING) need a vehicle actually big enough to do
		// them — compared directly by payload capacity (maxWeightKg). Skipped (no restriction)
		// when either side's vehicle type isn't known — a "my whole fleet" posting, or a request
		// with no recorded size — rather than guessing and wrongly excluding a real match.
		// Package/car/bike (TRANSFERSERVICE) never needs the whole vehicle, so it's never
		// size-gated. vehicleMatchPercent scores how closely sized the match is — exact capacity =
		// 100%, dropping as the posting's vehicle is increasingly oversized for the job — null
		// (excluded from the overall average) when the check doesn't apply.
		boolean isWholeVehicleService = candidate.getServiceType() == serviceTypeEnum.BOOKVEHICLE
				|| candidate.getServiceType() == serviceTypeEnum.HOMESHIFTING;
		boolean vehicleSizeOk = true;
		Integer vehicleMatchPercent = null;
		if (isWholeVehicleService && ctx.postingVehicle != null && candidate.getVendorPickupVehicle() != null) {
			Integer requiredWeightKg = candidate.getVendorPickupVehicle().getMaxWeightKg();
			Integer postingWeightKg = ctx.postingVehicle.getMaxWeightKg();
			if (requiredWeightKg != null && postingWeightKg != null && postingWeightKg > 0) {
				vehicleSizeOk = requiredWeightKg <= postingWeightKg
						&& requiredWeightKg >= MIN_VEHICLE_CAPACITY_RATIO * postingWeightKg;
				if (vehicleSizeOk) {
					vehicleMatchPercent = (int) Math.round(
							100.0 * Math.min(1.0, (double) requiredWeightKg / postingWeightKg));
				}
			}
		}

		// Same permissive fallback as above — skipped (no restriction, and excluded from the
		// percent average) when either distance is unknown, rather than guessing and wrongly
		// dropping a real match over missing data.
		boolean rideDistanceOk = true;
		Integer distanceRatioPercent = null;
		if (ctx.postingDistanceKm != null && ctx.postingDistanceKm > 0 && candidate.getDistanceKm() != null) {
			double ratio = candidate.getDistanceKm() / ctx.postingDistanceKm;
			rideDistanceOk = ratio >= MIN_RIDE_DISTANCE_RATIO;
			distanceRatioPercent = (int) Math.round(Math.max(0, Math.min(100, ratio * 100.0)));
		}

		List<MatchOutcome> outcomes = new ArrayList<>(2);

		if (vehicleSizeOk && rideDistanceOk && directionOk && (nearOrigin || nearDest || onRoute || nearWaypoint)) {
			// Closest of whichever reasons actually matched, each scored against its own radius.
			double bestDistanceKm = Double.MAX_VALUE;
			double bestRadiusKm = ENDPOINT_RADIUS_KM;
			if (nearOrigin && distToSource < bestDistanceKm) {
				bestDistanceKm = distToSource;
				bestRadiusKm = ENDPOINT_RADIUS_KM;
			}
			if (nearDest && distToDest < bestDistanceKm) {
				bestDistanceKm = distToDest;
				bestRadiusKm = ENDPOINT_RADIUS_KM;
			}
			if (nearWaypoint && distToWaypoint < bestDistanceKm) {
				bestDistanceKm = distToWaypoint;
				bestRadiusKm = ENDPOINT_RADIUS_KM;
			}
			if (onRoute && distToRoute < bestDistanceKm) {
				bestDistanceKm = distToRoute;
				bestRadiusKm = ROUTE_CORRIDOR_KM;
			}
			int overallPercent = averagePercent(
					percentFromDistance(bestDistanceKm, bestRadiusKm), distanceRatioPercent, vehicleMatchPercent);
			outcomes.add(new MatchOutcome("POSTING_ROUTE", bestDistanceKm, overallPercent, vehicleMatchPercent));
		}

		// ---- Return leg (opt-in only): pickup near/along the same corridor toward the posting's
		// destination (or near a waypoint on it), but this time the candidate must also drop back
		// off near the posting's own starting point — an actual to->from job, not just any pickup
		// near the destination.
		if (vehicleSizeOk && rideDistanceOk && posting.isReturnTrip() && ctx.fromLat != null && ctx.fromLng != null
				&& (nearDest || onRoute || nearWaypoint)) {
			if (candidateDestLat != null && candidateDestLng != null) {
				double distDropToOrigin = GeoUtils.haversineKm(candidateDestLat, candidateDestLng, ctx.fromLat, ctx.fromLng);
				if (distDropToOrigin <= ENDPOINT_RADIUS_KM) {
					double pickupDistanceKm = distToDest;
					double pickupRadiusKm = ENDPOINT_RADIUS_KM;
					if (onRoute && distToRoute < pickupDistanceKm) {
						pickupDistanceKm = distToRoute;
						pickupRadiusKm = ROUTE_CORRIDOR_KM;
					}
					if (nearWaypoint && distToWaypoint < pickupDistanceKm) {
						pickupDistanceKm = distToWaypoint;
						pickupRadiusKm = ENDPOINT_RADIUS_KM;
					}
					int pickupPercent = percentFromDistance(pickupDistanceKm, pickupRadiusKm);
					int dropPercent = percentFromDistance(distDropToOrigin, ENDPOINT_RADIUS_KM);
					double combinedDistanceKm = (pickupDistanceKm + distDropToOrigin) / 2.0;
					int combinedPercent = averagePercent(
							pickupPercent, dropPercent, distanceRatioPercent, vehicleMatchPercent);
					outcomes.add(new MatchOutcome("RETURN_TRIP", combinedDistanceKm, combinedPercent, vehicleMatchPercent));
				}
			}
		}

		return outcomes;
	}

	// A request matching more than once (across postings, or forward+return on the same posting)
	// keeps its RETURN_TRIP tag over POSTING_ROUTE (a vendor-confirmed return trip is a stronger
	// signal than incidental route proximity), or whichever match scores higher within the same
	// tag — not whichever is physically closer, since a closer-but-lower-scoring match (e.g. worse
	// vehicle-size fit) would be a confusing number to show the vendor. Generic over what `key`
	// represents: a request id when accumulating one vendor's best match per request
	// (recomputeAndPersistForVendor), or a vendor id when accumulating one request's best match
	// per vendor (recomputeForRequest).
	private void mergeOutcome(Map<Long, MatchOutcome> bestByKey, Long key, MatchOutcome outcome) {
		MatchOutcome existing = bestByKey.get(key);
		boolean isBetter = existing == null
				|| ("RETURN_TRIP".equals(outcome.matchType) && !"RETURN_TRIP".equals(existing.matchType))
				|| (outcome.matchType.equals(existing.matchType) && outcome.scorePercent > existing.scorePercent);
		if (isBetter) {
			bestByKey.put(key, outcome);
		}
	}

	private VendorAvailabilityMatch toMatchRow(Long vendorId, TransferRequestDetails candidate, MatchOutcome outcome, LocalDateTime now) {
		VendorAvailabilityMatch row = new VendorAvailabilityMatch();
		row.setVendorId(vendorId);
		row.setTransferRequestId(candidate.getId());
		row.setMatchType(outcome.matchType);
		row.setMatchDistanceKm(Math.round(outcome.distanceKm * 10) / 10.0);
		row.setMatchScorePercent(outcome.scorePercent);
		row.setVehicleMatchPercent(outcome.vehicleMatchPercent);
		row.setRequestCreatedDate(candidate.getRequestCreatedDate());
		row.setPickupDate(candidate.getPickupDate());
		row.setInstantBooking(candidate.getInstantBooking());
		row.setComputedAt(now);
		return row;
	}

	private int percentFromDistance(double distanceKm, double radiusKm) {
		return (int) Math.round(Math.max(0, Math.min(100, 100.0 * (1 - distanceKm / radiusKm))));
	}

	// Averages whichever score components actually apply — proximity is always present, distance-
	// ratio and vehicle-size are null (and skipped) whenever that check wasn't applicable, rather
	// than dragging the average down with a meaningless default.
	private int averagePercent(Integer... components) {
		int sum = 0;
		int count = 0;
		for (Integer c : components) {
			if (c != null) {
				sum += c;
				count++;
			}
		}
		return count == 0 ? 0 : (int) Math.round((double) sum / count);
	}
}
