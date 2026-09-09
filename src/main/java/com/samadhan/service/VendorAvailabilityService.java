package com.samadhan.service;

import java.util.List;

import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VendorAvailability;
import com.samadhan.request.VendorAvailabilityRequest;

public interface VendorAvailabilityService {

	VendorAvailability postAvailability(VendorAvailabilityRequest request);

	List<VendorAvailability> getActiveForVendor(Long vendorId);

	void cancelAvailability(Long vendorId, Long availabilityId);

	// Requests matching an active posting's route: near the destination or along the from->to
	// driving route (tagged matchType=POSTING_ROUTE), or near the posting's own starting point
	// (tagged RETURN_TRIP) — see VendorAvailabilityServiceImpl.getRequestsMatchingAvailability.
	// matchType/matchDistanceKm are transient fields set on each returned TransferRequestDetails.
	List<TransferRequestDetails> getRequestsMatchingAvailability(Long vendorId);
}
