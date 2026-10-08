package com.samadhan.repository;

// Projection for TransferRequestRepository#findVehiclePerformanceByVendor — column aliases in
// that native query bind to these getters by name.
public interface VehiclePerformanceProjection {
	Long getVehicleId();
	String getVehicleNumber();
	Integer getVehicleType();
	Integer getVehicleCategory();
	Boolean getIsActive();
	Long getCurrentRideId();
	Integer getCompletedRides();
	Integer getOngoingRides();
	Double getTotalRevenue();
	Double getTotalPlatformFee();
	Double getAvgRating();
	Integer getRatingCount();
}
