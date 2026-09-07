package com.samadhan.service;

import java.util.List;

import com.samadhan.entity.VendorAvailability;
import com.samadhan.request.VendorAvailabilityRequest;

public interface VendorAvailabilityService {

	VendorAvailability postAvailability(VendorAvailabilityRequest request);

	List<VendorAvailability> getActiveForVendor(Long vendorId);

	void cancelAvailability(Long vendorId, Long availabilityId);
}
