package com.samadhan.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.Vehicle;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.exception.ConflictException;

public interface VehicleService {

	List<Vehicle> getAllVehiclesByVendor(Long vendorId, boolean isActive);

	Vehicle createVehicle(String vehicleNumber, String vehicleContactNumber, String currentLocation,
			VendorPickupVehicleEnum vendorVehicle, VehicleCategoryEnum vehicleCategory, String vehicleLatitude,
			String vehicleLongitude, String fcmToken, Long transferVendorId, MultipartFile rcFile);

	Vehicle updateLocation(String address, Long vehicleId);

	Vehicle loginVehicle(String userName, String password);

	Vehicle registerVehicle(Vehicle vehicle);

	Vehicle deactivateVehicle(Long vehicleId);

	// Hard delete — permanently removes the row. Rejects (ConflictException) if the vehicle has
	// existing transfer/ride history, so history isn't silently orphaned or lost; callers should
	// use deactivateVehicle instead in that case.
	void deleteVehicle(Long vehicleId) throws ConflictException;

}
