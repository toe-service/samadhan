package com.samadhan.repository;

// Projection for VehicleRepository#searchForAdmin — a flat vehicle-plus-owning-vendor row for
// the admin panel's "find a vehicle by plate number" search, independent of which vendor's
// fleet it's in.
public interface AdminVehicleSearchRow {
	Long getId();
	String getVehicleNumber();
	String getVehicleContactNumber();
	Boolean getIsActive();
	Long getVendorId();
	String getVendorName();
	Boolean getIsIndividual();
}
