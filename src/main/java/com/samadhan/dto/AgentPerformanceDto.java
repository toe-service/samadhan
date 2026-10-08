package com.samadhan.dto;

public class AgentPerformanceDto {

	private Long driverId;
	private String driverName;
	private String driverContactNumber;
	private Boolean isActive;
	// "ON_DUTY" when currentRideId is non-null, else "IDLE".
	private String status;
	private Long currentRideId;
	private Integer completedHandoffs;
	private Integer ongoingHandoffs;
	private Double totalRideValue;

	public Long getDriverId() {
		return driverId;
	}

	public void setDriverId(Long driverId) {
		this.driverId = driverId;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverContactNumber() {
		return driverContactNumber;
	}

	public void setDriverContactNumber(String driverContactNumber) {
		this.driverContactNumber = driverContactNumber;
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

	public Integer getCompletedHandoffs() {
		return completedHandoffs;
	}

	public void setCompletedHandoffs(Integer completedHandoffs) {
		this.completedHandoffs = completedHandoffs;
	}

	public Integer getOngoingHandoffs() {
		return ongoingHandoffs;
	}

	public void setOngoingHandoffs(Integer ongoingHandoffs) {
		this.ongoingHandoffs = ongoingHandoffs;
	}

	public Double getTotalRideValue() {
		return totalRideValue;
	}

	public void setTotalRideValue(Double totalRideValue) {
		this.totalRideValue = totalRideValue;
	}
}
