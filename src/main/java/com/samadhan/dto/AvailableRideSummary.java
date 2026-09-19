package com.samadhan.dto;

import java.time.LocalDate;

// One route+date group for GET /vendor/available-rides — backs the customer app's "Available
// Rides" tab ("N vehicles traveling City A -> City B on <date>, 10% off"). discountPercent is
// advertising copy only, not enforced at booking; see VendorAvailabilityServiceImpl for the
// single named constant it comes from.
public class AvailableRideSummary {

	private String fromCity;
	private String toCity;
	private LocalDate expectedDate;
	private long vehicleCount;
	private int discountPercent;

	public String getFromCity() {
		return fromCity;
	}

	public void setFromCity(String fromCity) {
		this.fromCity = fromCity;
	}

	public String getToCity() {
		return toCity;
	}

	public void setToCity(String toCity) {
		this.toCity = toCity;
	}

	public LocalDate getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(LocalDate expectedDate) {
		this.expectedDate = expectedDate;
	}

	public long getVehicleCount() {
		return vehicleCount;
	}

	public void setVehicleCount(long vehicleCount) {
		this.vehicleCount = vehicleCount;
	}

	public int getDiscountPercent() {
		return discountPercent;
	}

	public void setDiscountPercent(int discountPercent) {
		this.discountPercent = discountPercent;
	}
}
