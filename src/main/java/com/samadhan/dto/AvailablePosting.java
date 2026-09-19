package com.samadhan.dto;

import java.time.LocalDate;

// One individual vendor posting for GET /vendor/available-rides/postings — backs the "pick a
// specific vendor" drill-down after a user taps a route summary (AvailableRideSummary) in the
// customer app's "Available Rides" tab. Deliberately narrow: only what's needed to identify and
// select a posting, not the vendor's full profile.
public class AvailablePosting {

	private Long availabilityId;
	private Long vendorId;
	private String vendorName;
	private String vehicleType;
	private String vehicleCategory;
	private LocalDate expectedDate;

	public Long getAvailabilityId() {
		return availabilityId;
	}

	public void setAvailabilityId(Long availabilityId) {
		this.availabilityId = availabilityId;
	}

	public Long getVendorId() {
		return vendorId;
	}

	public void setVendorId(Long vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
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

	public LocalDate getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(LocalDate expectedDate) {
		this.expectedDate = expectedDate;
	}
}
