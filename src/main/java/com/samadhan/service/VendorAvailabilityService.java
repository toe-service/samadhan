package com.samadhan.service;

import java.util.List;

import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VendorAvailability;
import com.samadhan.request.VendorAvailabilityRequest;

public interface VendorAvailabilityService {

	VendorAvailability postAvailability(VendorAvailabilityRequest request);

	List<VendorAvailability> getActiveForVendor(Long vendorId);

	void cancelAvailability(Long vendorId, Long availabilityId);

	// Requests near an active posting's toLocation, on that posting's expectedDate — see
	// TransferRequestRepository.getRequestsMatchingVendorAvailability for the matching rule.
	List<TransferRequestDetails> getRequestsMatchingAvailability(Long vendorId);
}
