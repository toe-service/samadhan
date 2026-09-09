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

	// For each active posting, finds PENDING/unassigned requests near the posting's destination,
	// near its starting point, or along its from->to driving route, and tags each one with why it
	// matched. A request matching more than one posting/reason keeps its closest (smallest
	// distance) match.
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

			List<double[]> routePoints = GeoUtils.decodePolyline(posting.getRoutePolyline());

			for (TransferRequestDetails candidate : candidates) {
				Double srcLat = GeoUtils.parseCoord(candidate.getSourceLatitude());
				Double srcLng = GeoUtils.parseCoord(candidate.getSourceLongitude());
				if (srcLat == null || srcLng == null) {
					continue;
				}

				Double distToSource = (fromLat != null && fromLng != null)
						? GeoUtils.haversineKm(srcLat, srcLng, fromLat, fromLng) : null;
				double distToDest = GeoUtils.haversineKm(srcLat, srcLng, toLat, toLng);
				Double distToRoute = routePoints.isEmpty() ? null
						: GeoUtils.minDistanceToPolylineKm(srcLat, srcLng, routePoints);

				boolean nearSource = distToSource != null && distToSource <= ENDPOINT_RADIUS_KM;
				boolean nearDest = distToDest <= ENDPOINT_RADIUS_KM;
				boolean onRoute = distToRoute != null && distToRoute <= ROUTE_CORRIDOR_KM;

				if (!nearSource && !nearDest && !onRoute) {
					continue;
				}

				// Pickup right at the posting's own starting point reads as a return-trip
				// opportunity; everything else (destination or along the corridor) is the
				// vendor's posted route itself.
				String matchType = nearSource ? "RETURN_TRIP" : "POSTING_ROUTE";
				double distanceKm = nearSource ? distToSource : (onRoute ? distToRoute : distToDest);

				TransferRequestDetails existing = bestMatches.get(candidate.getId());
				if (existing == null || distanceKm < existing.getMatchDistanceKm()) {
					candidate.setMatchType(matchType);
					candidate.setMatchDistanceKm(Math.round(distanceKm * 10) / 10.0);
					bestMatches.put(candidate.getId(), candidate);
				}
			}
		}

		return new ArrayList<>(bestMatches.values());
	}
}
