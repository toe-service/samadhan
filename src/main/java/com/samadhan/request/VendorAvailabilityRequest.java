package com.samadhan.request;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorAvailabilityRequest {
	public Long vendorId;
	public String userContact;
	public String fromLocation;
	public String fromLatitude;
	public String fromLongitude;
	public String toLocation;
	public String toLatitude;
	public String toLongitude;
	public LocalDate expectedDate;
	public String vehicleType;
	public String vehicleCategory;
	public String vehicleNumber;
	public Boolean returnTrip;
}
