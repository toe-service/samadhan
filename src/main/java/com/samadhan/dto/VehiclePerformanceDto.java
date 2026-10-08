package com.samadhan.dto;

public class VehiclePerformanceDto {

	private Long vehicleId;
	private String vehicleNumber;
	private String vehicleType;
	private String vehicleCategory;
	private Boolean isActive;
	// "ON_RIDE" when currentRideId is non-null, else "IDLE".
	private String status;
	private Long currentRideId;
	private Integer completedRides;
	private Integer ongoingRides;
	private Double totalRevenue;
	private Double totalPlatformFee;
	private Double avgRating;
	private Integer ratingCount;

	public Long getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Long vehicleId) {
		this.vehicleId = vehicleId;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public String getVehicleCategory() {
		return vehicleCategory;
	}

	public void setVehicleCategory(String vehicleCategory) {
		this.vehicleCategory = vehicleCategory;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getCurrentRideId() {
		return currentRideId;
	}

	public void setCurrentRideId(Long currentRideId) {
		this.currentRideId = currentRideId;
	}

	public Integer getCompletedRides() {
		return completedRides;
	}

	public void setCompletedRides(Integer completedRides) {
		this.completedRides = completedRides;
	}

	public Integer getOngoingRides() {
		return ongoingRides;
	}

	public void setOngoingRides(Integer ongoingRides) {
		this.ongoingRides = ongoingRides;
	}

	public Double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(Double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public Double getTotalPlatformFee() {
		return totalPlatformFee;
	}

	public void setTotalPlatformFee(Double totalPlatformFee) {
		this.totalPlatformFee = totalPlatformFee;
	}

	public Double getAvgRating() {
		return avgRating;
	}

	public void setAvgRating(Double avgRating) {
		this.avgRating = avgRating;
	}

	public Integer getRatingCount() {
		return ratingCount;
	}

	public void setRatingCount(Integer ratingCount) {
		this.ratingCount = ratingCount;
	}
}
