package com.samadhan.repository;

// Projection for TransferRequestRepository#countAllRidesByStatus — platform-wide (no vendor
// scoping), unlike RideStatusCounts which is one vendor's eligible-feed counts. transfer_status
// values per rideStatusEnum: 0 PENDING, 1 ACCEPTED, 3/5/6 in-progress bucket (READYFORPICKUP/
// VEHICLEASSIGNED/ONGOING), 8 COMPLETED, 9 CANCELLED.
public interface AdminRideStatusCounts {
	Long getTotal();
	Long getPending();
	Long getAccepted();
	Long getOngoing();
	Long getCompleted();
	Long getCancelled();
}
