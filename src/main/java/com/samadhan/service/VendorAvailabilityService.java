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
	// (tagged RETURN_TRIP) — see VendorAvailabilityServiceImpl. matchType/matchDistanceKm/
	// matchScorePercent/vehicleMatchPercent are transient fields set on each returned
	// TransferRequestDetails, copied from the precomputed vendor_availability_match table (see
	// recomputeForRequest/recomputeAndPersistForVendor below) rather than computed live on every
	// read. vehicleMatchPercent is null whenever the posting has no specific vehicleType, the
	// request isn't a whole-vehicle service, or either side's vehicle size isn't known.
	MatchingRequestsResponse getRequestsMatchingAvailability(Long vendorId, int page, int size, LocalDate pickupDate);

	// Recomputes and persists this ONE vendor's full match set (against every currently pending/
	// unassigned request) — called whenever that vendor's own postings change.
	void recomputeAndPersistForVendor(Long vendorId);

	// Recomputes and persists match rows for this ONE request against every vendor's active
	// postings — called when a request newly enters (or re-enters, e.g. after a cancel) the
	// pending/unassigned pool.
	void recomputeForRequest(Long transferRequestId);

	// Removes any persisted match rows for this request — called when it leaves the pending/
	// unassigned pool (a vehicle gets assigned, or it's otherwise accepted).
	void invalidateForRequest(Long transferRequestId);

	// One-time seed: if vendor_availability_match is empty (e.g. right after this feature is
	// first deployed), populates it from every vendor that currently has an active posting.
	// No-ops immediately once the table has any rows, so it's cheap to call on every app startup.
	void bootstrapMatchesIfEmpty();

}
