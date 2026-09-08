package com.samadhan.repository;

// Projection for TransferRequestRepository#countRidesByStatusForVendor — column aliases in
// that native query (total/pending/accepted/ongoing) bind to these getters by name.
public interface RideStatusCounts {
	Long getTotal();
	Long getPending();
	Long getAccepted();
	Long getOngoing();
}
