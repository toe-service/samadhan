package com.samadhan.repository;

import java.time.LocalDate;

// Projection for VendorAvailabilityRepository#findAvailableRideSummaries/
// findAvailableRideSummariesForCityOnDate — column aliases in those native queries
// (from_city/to_city/expected_date/vehicle_count) bind to these getters by name.
public interface AvailableRideSummaryProjection {
	String getFromCity();
	String getToCity();
	LocalDate getExpectedDate();
	Long getVehicleCount();
}
