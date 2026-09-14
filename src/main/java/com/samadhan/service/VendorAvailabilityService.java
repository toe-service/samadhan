package com.samadhan.service;

import java.time.LocalDate;
import java.util.List;

import com.samadhan.dto.MatchingRequestsResponse;
import com.samadhan.entity.VendorAvailability;
import com.samadhan.request.VendorAvailabilityRequest;

public interface VendorAvailabilityService {

	VendorAvailability postAvailability(VendorAvailabilityRequest request);

	VendorAvailability updateAvailability(Long vendorId, Long availabilityId, VendorAvailabilityRequest request);

	List<VendorAvailability> getActiveForVendor(Long vendorId);

	void cancelAvailability(Long vendorId, Long availabilityId);

	// Requests matching an active posting's route: near the destination or along the from->to
	// driving route (tagged matchType=POSTING_ROUTE), or near the posting's own starting point
	// (tagged RETURN_TRIP) — see VendorAvailabilityServiceImpl.getRequestsMatchingAvailability.
	// matchType/matchDistanceKm are transient fields set on each returned TransferRequestDetails.
	// The full match set is computed every call (it's a Java-side geo computation, not a plain
	// DB query — see the impl), optionally narrowed by pickupDate (same semantics as the main
	// rides feed — exact date match, or today's Immediate bookings when filtering on today),
	// then paginated in memory, same pattern as TransferRequestServiceImpl#buildPagedResponseInMemory.
	MatchingRequestsResponse getRequestsMatchingAvailability(Long vendorId, int page, int size, LocalDate pickupDate);
}
