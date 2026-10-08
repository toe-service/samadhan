package com.samadhan.repository;

// Projection for TransferRequestRepository#findAgentPerformanceByVendor — column aliases in
// that native query bind to these getters by name.
public interface AgentPerformanceProjection {
	Long getDriverId();
	String getDriverName();
	String getDriverContactNumber();
	Boolean getIsActive();
	Long getCurrentRideId();
	Integer getCompletedHandoffs();
	Integer getOngoingHandoffs();
	Double getTotalRideValue();
}
