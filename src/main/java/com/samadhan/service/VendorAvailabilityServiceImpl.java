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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.samadhan.dto.GeoPoint;
import com.samadhan.dto.MatchingRequestsResponse;
import com.samadhan.dto.RouteRequest;
import com.samadhan.dto.RouteResponse;
import com.samadhan.dto.RouteWaypoint;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorAvailability;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.exception.ResourceNotFoundException;
import com.samadhan.exception.SubscriptionSuspendedException;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
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
	// every one of those is the dominant cost of computeMatches. Thinned to this spacing (still far
	// tighter than ROUTE_CORRIDOR_KM, so no meaningful accuracy loss) via GeoUtils.simplifyPolyline.
	private static final double ROUTE_SIMPLIFY_SPACING_KM = 2.0;
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
	RouteService routeService;

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
			} catch (Exception ex) {
				// Best-effort: without a cached route, matching still works via endpoint-radius checks.
				log.warn("Could not compute route for vendor availability {}: {}", saved.getId(), ex.getMessage());
			}
		}

		return saved;
	}

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
			} catch (Exception ex) {
				log.warn("Could not compute route for vendor availability {}: {}", availability.getId(), ex.getMessage());
			}
		} else {
			availability.setRoutePolyline(null);
		}

		return vendorAvailabilityRepository.save(availability);
	}

	@Override
	public List<VendorAvailability> getActiveForVendor(Long vendorId) {
		return vendorAvailabilityRepository.findByTransferVendorIdAndActiveTrueOrderByExpectedDateAsc(vendorId);
	}

	@Override
	public void cancelAvailability(Long vendorId, Long availabilityId) {
		VendorAvailability availability = vendorAvailabilityRepository.findById(availabilityId)
				.orElseThrow(() -> new ResourceNotFoundException("Availability not found: " + availabilityId));

		if (availability.getTransferVendor() == null || !vendorId.equals(availability.getTransferVendor().getId())) {
			throw new AccessDeniedException("You are not authorized to cancel this availability posting");
		}

		availability.setActive(false);
		vendorAvailabilityRepository.save(availability);
	}

	// For each active posting, finds PENDING/unassigned requests either along the posting's own
	// from->to route (always), or — only when the vendor explicitly opted into a return trip on
	// that posting — along the reverse to->from route as well, so an empty backhaul leg isn't
	// silently assumed just because a pickup happens to be near fromLocation. Each match is
	// tagged with why it matched and a 0-100 percent score; a request matching more than once
	// keeps its RETURN_TRIP tag over POSTING_ROUTE (a vendor-confirmed return trip is a stronger
	// signal than incidental route proximity), or its closest match within the same tag.
	@Override
	public MatchingRequestsResponse getRequestsMatchingAvailability(Long vendorId, int page, int size, LocalDate pickupDate) {
		int safePage = Math.max(page, 0);
		int safeSize = Math.max(size, 1);
		List<TransferRequestDetails> allMatches = computeMatches(vendorId);

		// Same "exact date, or today's Immediate bookings when filtering on today" semantics as
		// the main rides feed (TransferRequestRepository#showRidestoVendorsPaged) — applied here,
		// before pagination, so totalElements/totalPages reflect the filtered count rather than
		// the filter being applied only to whichever page happened to come back.
		if (pickupDate != null) {
			boolean isToday = pickupDate.isEqual(LocalDate.now());
			allMatches = allMatches.stream()
					.filter(m -> pickupDate.equals(m.getPickupDate())
							|| (isToday && Boolean.TRUE.equals(m.getInstantBooking())))
					.collect(Collectors.toList());
		}

		int fromIndex = Math.min(safePage * safeSize, allMatches.size());
		int toIndex = Math.min(fromIndex + safeSize, allMatches.size());

		MatchingRequestsResponse response = new MatchingRequestsResponse();
		response.setMatches(allMatches.subList(fromIndex, toIndex));
		response.setTotalElements(allMatches.size());
		response.setTotalPages((int) Math.ceil(allMatches.size() / (double) safeSize));
		response.setPage(safePage);
		response.setSize(safeSize);
		return response;
	}

	// The actual matching computation, unpaginated — split out from
	// getRequestsMatchingAvailability so pagination is a thin, separate concern layered on top.
	private List<TransferRequestDetails> computeMatches(Long vendorId) {
		List<VendorAvailability> postings =
				vendorAvailabilityRepository.findByTransferVendorIdAndActiveTrueOrderByExpectedDateAsc(vendorId);
		if (postings.isEmpty()) {
			return new ArrayList<>();
		}

		// Fetched once for all of this vendor's postings, not once per posting — the previous
		// per-posting bounding-box query (findPendingUnassignedInBoundingBox) can't use an index
		// (source_latitude/longitude are TEXT, so its WHERE clause needs a CAST/TRIM per row) and
		// is effectively a full scan of every pending request. Running that once-per-posting
		// multiplied an already-expensive scan by the vendor's active-posting count. The bounding
		// box itself is now applied per-posting in Java below, against this shared pool.
		LocalDate latestExpectedDate = postings.stream()
				.map(VendorAvailability::getExpectedDate)
				.filter(java.util.Objects::nonNull)
				.max(Comparator.naturalOrder())
				.orElse(LocalDate.now());
		List<TransferRequestDetails> candidatePool =
				transferRequestRepository.findPendingUnassignedUpTo(latestExpectedDate);

		Map<Long, TransferRequestDetails> bestMatches = new LinkedHashMap<>();

		for (VendorAvailability posting : postings) {
			Double toLat = GeoUtils.parseCoord(posting.getToLatitude());
			Double toLng = GeoUtils.parseCoord(posting.getToLongitude());
			if (toLat == null || toLng == null) {
				continue;
			}
			Double fromLat = GeoUtils.parseCoord(posting.getFromLatitude());
			Double fromLng = GeoUtils.parseCoord(posting.getFromLongitude());

			double minLat = (fromLat != null) ? Math.min(fromLat, toLat) : toLat;
			double maxLat = (fromLat != null) ? Math.max(fromLat, toLat) : toLat;
			double minLng = (fromLng != null) ? Math.min(fromLng, toLng) : toLng;
			double maxLng = (fromLng != null) ? Math.max(fromLng, toLng) : toLng;

			// Vendor-specified intermediate stops (see postAvailability) — widen the bounding box
			// to include them (a detour via a waypoint can sit well outside the straight-line
			// from->to box) and keep their coordinates for the direct waypoint-proximity check
			// below, independent of whether routePolyline successfully bent through them.
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

			double boxMinLat = minLat - BOUNDING_BOX_BUFFER_DEG;
			double boxMaxLat = maxLat + BOUNDING_BOX_BUFFER_DEG;
			double boxMinLng = minLng - BOUNDING_BOX_BUFFER_DEG;
			double boxMaxLng = maxLng + BOUNDING_BOX_BUFFER_DEG;
			LocalDate postingExpectedDate = posting.getExpectedDate();
			List<TransferRequestDetails> candidates = new ArrayList<>();
			for (TransferRequestDetails c : candidatePool) {
				if (postingExpectedDate != null && c.getPickupDate() != null
						&& c.getPickupDate().isAfter(postingExpectedDate)) {
					continue;
				}
				Double cLat = GeoUtils.parseCoord(c.getSourceLatitude());
				Double cLng = GeoUtils.parseCoord(c.getSourceLongitude());
				if (cLat == null || cLng == null) {
					continue;
				}
				if (cLat < boxMinLat || cLat > boxMaxLat || cLng < boxMinLng || cLng > boxMaxLng) {
					continue;
				}
				candidates.add(c);
			}

			// Direction-agnostic — a straight-line distance to the nearest point on this road
			// corridor is the same whether the vendor is driving it from->to or to->from, so the
			// same decoded polyline serves both legs below. Simplified once per posting (not per
			// candidate) so the per-candidate distance check further down isn't paying for Google's
			// full vertex density.
			List<double[]> routePoints = GeoUtils.simplifyPolyline(
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

			// Straight-line, not the driving-route distance — always available regardless of
			// whether routePolyline was successfully computed (see postAvailability), and only
			// used as the yardstick for MIN_RIDE_DISTANCE_RATIO, not for precise distance checks.
			Double postingDistanceKm = (fromLat != null && fromLng != null)
					? GeoUtils.haversineKm(fromLat, fromLng, toLat, toLng) : null;

			for (TransferRequestDetails candidate : candidates) {
				Double srcLat = GeoUtils.parseCoord(candidate.getSourceLatitude());
				Double srcLng = GeoUtils.parseCoord(candidate.getSourceLongitude());
				if (srcLat == null || srcLng == null) {
					continue;
				}

				// ---- Forward leg (always evaluated): pickup near the posting's own starting
				// point (about to drive right past/through there), near its destination, near a
				// vendor-specified waypoint, or anywhere along the from->to corridor. nearOrigin/
				// nearWaypoint matter independently of onRoute — the route polyline is a
				// best-effort cache computed at posting time (see postAvailability) and can be
				// missing/failed, in which case onRoute is always false and these would otherwise
				// be the only remaining way to catch pickups at those points.
				Double distToSource = (fromLat != null && fromLng != null)
						? GeoUtils.haversineKm(srcLat, srcLng, fromLat, fromLng) : null;
				double distToDest = GeoUtils.haversineKm(srcLat, srcLng, toLat, toLng);
				Double distToRoute = routePoints.isEmpty() ? null
						: GeoUtils.minDistanceToPolylineKm(srcLat, srcLng, routePoints);
				Double distToWaypoint = null;
				for (double[] wp : waypointCoords) {
					double d = GeoUtils.haversineKm(srcLat, srcLng, wp[0], wp[1]);
					if (distToWaypoint == null || d < distToWaypoint) {
						distToWaypoint = d;
					}
				}
				boolean nearOrigin = distToSource != null && distToSource <= ENDPOINT_RADIUS_KM;
				boolean nearDest = distToDest <= ENDPOINT_RADIUS_KM;
				boolean onRoute = distToRoute != null && distToRoute <= ROUTE_CORRIDOR_KM;
				boolean nearWaypoint = distToWaypoint != null && distToWaypoint <= ENDPOINT_RADIUS_KM;

				// Whole-vehicle jobs (BOOKVEHICLE, HOMESHIFTING) need a vehicle actually big enough
				// to do them — compared directly by payload capacity (maxWeightKg), NOT the
				// "requested + next 2 larger" ladder used for live push-notification eligibility
				// (TransferRequestServiceImpl#isEligiblePendingRide / FireBaseMessagingService).
				// That 2-step cap exists there to limit notification spam to nearby vehicles, not
				// to define "can this vehicle physically do the job" — reusing it here wrongly
				// excluded a real match (e.g. a 20ft Open truck, 10000kg, sits several steps past
				// a 14ft Open request, 4000kg, once closer-capacity types like 17ft/15ft Open are
				// counted, even though 20ft obviously can carry a 14ft-sized load). Any posting
				// vehicle with capacity >= the request's is eligible, full stop. Skipped (no
				// restriction) when either side's vehicle type isn't known — a "my whole fleet"
				// posting, or a request with no recorded size — rather than guessing and wrongly
				// excluding a real match. Package/car/bike (TRANSFERSERVICE) never needs the whole
				// vehicle, so it's never size-gated. vehicleMatchPercent scores how closely sized
				// the match is — exact capacity = 100%, dropping as the posting's vehicle is
				// increasingly oversized for the job — null (excluded from the overall average)
				// when the check doesn't apply.
				boolean isWholeVehicleService = candidate.getServiceType() == serviceTypeEnum.BOOKVEHICLE
						|| candidate.getServiceType() == serviceTypeEnum.HOMESHIFTING;
				boolean vehicleSizeOk = true;
				Integer vehicleMatchPercent = null;
				if (isWholeVehicleService && postingVehicle != null && candidate.getVendorPickupVehicle() != null) {
					Integer requiredWeightKg = candidate.getVendorPickupVehicle().getMaxWeightKg();
					Integer postingWeightKg = postingVehicle.getMaxWeightKg();
					if (requiredWeightKg != null && postingWeightKg != null && postingWeightKg > 0) {
						vehicleSizeOk = requiredWeightKg <= postingWeightKg
								&& requiredWeightKg >= MIN_VEHICLE_CAPACITY_RATIO * postingWeightKg;
						if (vehicleSizeOk) {
							vehicleMatchPercent = (int) Math.round(
									100.0 * Math.min(1.0, (double) requiredWeightKg / postingWeightKg));
						}
					}
				}

				// Same permissive fallback as above — skipped (no restriction, and excluded from
				// the percent average) when either distance is unknown, rather than guessing and
				// wrongly dropping a real match over missing data.
				boolean rideDistanceOk = true;
				Integer distanceRatioPercent = null;
				if (postingDistanceKm != null && postingDistanceKm > 0 && candidate.getDistanceKm() != null) {
					double ratio = candidate.getDistanceKm() / postingDistanceKm;
					rideDistanceOk = ratio >= MIN_RIDE_DISTANCE_RATIO;
					distanceRatioPercent = (int) Math.round(Math.max(0, Math.min(100, ratio * 100.0)));
				}

				if (vehicleSizeOk && rideDistanceOk && (nearOrigin || nearDest || onRoute || nearWaypoint)) {
					// Closest of whichever reasons actually matched, each scored against its own
					// radius.
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
					considerMatch(bestMatches, candidate, "POSTING_ROUTE", bestDistanceKm, overallPercent);
				}

				// ---- Return leg (opt-in only): pickup near/along the same corridor toward the
				// posting's destination (or near a waypoint on it), but this time the candidate
				// must also drop back off near the posting's own starting point — an actual
				// to->from job, not just any pickup near the destination.
				if (vehicleSizeOk && rideDistanceOk && posting.isReturnTrip() && fromLat != null && fromLng != null
						&& (nearDest || onRoute || nearWaypoint)) {
					Double destLat = GeoUtils.parseCoord(candidate.getDestinationLatitude());
					Double destLng = GeoUtils.parseCoord(candidate.getDestinationLongitude());
					if (destLat != null && destLng != null) {
						double distDropToOrigin = GeoUtils.haversineKm(destLat, destLng, fromLat, fromLng);
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
							considerMatch(bestMatches, candidate, "RETURN_TRIP", combinedDistanceKm, combinedPercent);
						}
					}
				}
			}
		}

		// bestMatches is a LinkedHashMap, so without this its iteration order is just "whichever
		// posting/candidate happened to be processed first" — fine within a single posting (the
		// candidate query is itself latest-first) but not guaranteed once results from multiple
		// postings interleave. Sorting explicitly guarantees latest-first regardless.
		List<TransferRequestDetails> matches = new ArrayList<>(bestMatches.values());
		matches.sort(Comparator.comparing(
				TransferRequestDetails::getRequestCreatedDate,
				Comparator.nullsLast(Comparator.reverseOrder())));
		return matches;
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

	private void considerMatch(Map<Long, TransferRequestDetails> bestMatches, TransferRequestDetails candidate,
			String matchType, double distanceKm, int scorePercent) {
		TransferRequestDetails existing = bestMatches.get(candidate.getId());
		boolean isBetter = existing == null
				|| ("RETURN_TRIP".equals(matchType) && !"RETURN_TRIP".equals(existing.getMatchType()))
				|| (matchType.equals(existing.getMatchType()) && distanceKm < existing.getMatchDistanceKm());
		if (isBetter) {
			candidate.setMatchType(matchType);
			candidate.setMatchDistanceKm(Math.round(distanceKm * 10) / 10.0);
			candidate.setMatchScorePercent(scorePercent);
			bestMatches.put(candidate.getId(), candidate);
		}
	}
}
