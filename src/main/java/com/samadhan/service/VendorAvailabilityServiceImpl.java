package com.samadhan.service;

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
		availability.setActive(true);
		availability.setCreatedAt(LocalDateTime.now());

		VendorAvailability saved = vendorAvailabilityRepository.save(availability);

		Double fromLat = GeoUtils.parseCoord(request.fromLatitude);
		Double fromLng = GeoUtils.parseCoord(request.fromLongitude);
		Double toLat = GeoUtils.parseCoord(request.toLatitude);
		Double toLng = GeoUtils.parseCoord(request.toLongitude);

		if (fromLat != null && fromLng != null && toLat != null && toLng != null) {
			try {
				RouteResponse route = routeService.getRoute(new RouteRequest(
						new GeoPoint(fromLat, fromLng), new GeoPoint(toLat, toLng), "DRIVE", false, null));
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

			List<TransferRequestDetails> candidates = transferRequestRepository.findPendingUnassignedInBoundingBox(
					posting.getExpectedDate(),
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

				// ---- Forward leg (always evaluated): pickup near the posting's destination, or
				// anywhere along the from->to corridor.
				double distToDest = GeoUtils.haversineKm(srcLat, srcLng, toLat, toLng);
				Double distToRoute = routePoints.isEmpty() ? null
						: GeoUtils.minDistanceToPolylineKm(srcLat, srcLng, routePoints);
				boolean nearDest = distToDest <= ENDPOINT_RADIUS_KM;
				boolean onRoute = distToRoute != null && distToRoute <= ROUTE_CORRIDOR_KM;

				if (nearDest || onRoute) {
					boolean preferRoute = onRoute && (!nearDest || distToRoute <= distToDest);
					double distanceKm = preferRoute ? distToRoute : distToDest;
					double radiusKm = preferRoute ? ROUTE_CORRIDOR_KM : ENDPOINT_RADIUS_KM;
					considerMatch(bestMatches, candidate, "POSTING_ROUTE", distanceKm, percentFromDistance(distanceKm, radiusKm));
				}

				// ---- Return leg (opt-in only): pickup near/along the same corridor toward the
				// posting's destination, but this time the candidate must also drop back off near
				// the posting's own starting point — an actual to->from job, not just any pickup
				// near the destination.
				if (posting.isReturnTrip() && fromLat != null && fromLng != null && (nearDest || onRoute)) {
					Double destLat = GeoUtils.parseCoord(candidate.getDestinationLatitude());
					Double destLng = GeoUtils.parseCoord(candidate.getDestinationLongitude());
					if (destLat != null && destLng != null) {
						double distDropToOrigin = GeoUtils.haversineKm(destLat, destLng, fromLat, fromLng);
						if (distDropToOrigin <= ENDPOINT_RADIUS_KM) {
							boolean preferRoute = onRoute && (!nearDest || distToRoute <= distToDest);
							double pickupDistanceKm = preferRoute ? distToRoute : distToDest;
							double pickupRadiusKm = preferRoute ? ROUTE_CORRIDOR_KM : ENDPOINT_RADIUS_KM;
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
