package com.samadhan.repository;

import java.time.LocalDate;

// Projection for VendorAvailabilityRepository#findAvailablePostings — column aliases in that
// native query bind to these getters by name, same convention as AvailableRideSummaryProjection.
public interface AvailablePostingProjection {
	Long getAvailabilityId();
	Long getVendorId();
	String getVendorName();
	String getVehicleType();
	String getVehicleCategory();
	LocalDate getExpectedDate();
}
