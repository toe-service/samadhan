package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.samadhan.dto.GeoPoint;
import com.samadhan.dto.RouteRequest;
import com.samadhan.dto.RouteResponse;
import com.samadhan.dto.RouteWaypoint;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorAvailability;
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
	// Bounding-box padding around the posting's endpoints, applied before precise distance checks.
	private static final double BOUNDING_BOX_BUFFER_DEG = 0.5;
	// Matching used to require pickup_date == expected_date exactly, which missed genuinely
	// on-route candidates like an "Immediate" (today) pickup against a posting for a few days
	// out — the vendor could easily pick that up en route. The window runs from today (not
	// expected_date - N — a pickup dated before today can't exist for a PENDING request anyway,
	// and "immediate" ones are always dated today) through this many days after expected_date,
	// to also catch a short return window once the vendor has actually arrived.
	private static final int DATE_WINDOW_AFTER_EXPECTED_DAYS = 2;

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
	public List<TransferRequestDetails> getRequestsMatchingAvailability(Long vendorId) {
		List<VendorAvailability> postings =
				vendorAvailabilityRepository.findByTransferVendorIdAndActiveTrueOrderByExpectedDateAsc(vendorId);

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

			LocalDate today = LocalDate.now();
			LocalDate maxPickupDate = posting.getExpectedDate().plusDays(DATE_WINDOW_AFTER_EXPECTED_DAYS);
			List<TransferRequestDetails> candidates = transferRequestRepository.findPendingUnassignedInBoundingBox(
					today, maxPickupDate,
					minLat - BOUNDING_BOX_BUFFER_DEG, maxLat + BOUNDING_BOX_BUFFER_DEG,
					minLng - BOUNDING_BOX_BUFFER_DEG, maxLng + BOUNDING_BOX_BUFFER_DEG);

			// Direction-agnostic — a straight-line distance to the nearest point on this road
			// corridor is the same whether the vendor is driving it from->to or to->from, so the
			// same decoded polyline serves both legs below.
			List<double[]> routePoints = GeoUtils.decodePolyline(posting.getRoutePolyline());

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

				if (nearOrigin || nearDest || onRoute || nearWaypoint) {
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
					considerMatch(bestMatches, candidate, "POSTING_ROUTE", bestDistanceKm, percentFromDistance(bestDistanceKm, bestRadiusKm));
				}

				// ---- Return leg (opt-in only): pickup near/along the same corridor toward the
				// posting's destination (or near a waypoint on it), but this time the candidate
				// must also drop back off near the posting's own starting point — an actual
				// to->from job, not just any pickup near the destination.
				if (posting.isReturnTrip() && fromLat != null && fromLng != null && (nearDest || onRoute || nearWaypoint)) {
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
							int combinedPercent = (pickupPercent + dropPercent) / 2;
							considerMatch(bestMatches, candidate, "RETURN_TRIP", combinedDistanceKm, combinedPercent);
						}
					}
				}
			}
		}

		return new ArrayList<>(bestMatches.values());
	}

	private int percentFromDistance(double distanceKm, double radiusKm) {
		return (int) Math.round(Math.max(0, Math.min(100, 100.0 * (1 - distanceKm / radiusKm))));
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
