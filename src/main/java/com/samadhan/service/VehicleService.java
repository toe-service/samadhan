package com.samadhan.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.Vehicle;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;

public interface VehicleService {

	List<Vehicle> getAllVehiclesByVendor(Long vendorId, boolean isActive);

	Vehicle createVehicle(String vehicleNumber, String vehicleContactNumber, String currentLocation,
			VendorPickupVehicleEnum vendorVehicle, VehicleCategoryEnum vehicleCategory, String vehicleLatitude,
			String vehicleLongitude, String fcmToken, Long transferVendorId, MultipartFile rcFile);

	Vehicle updateLocation(String address, Long vehicleId, String lat, String lng);

	// lat/lng are optional — when present (a real vehicle-app login), refreshes the vehicle's live
	// position (vehicle_latitude/vehicle_longitude, plus the reverse-geocoded currentLocation)
	// alongside authenticating it. Pass null/null from callers that only need to verify
	// credentials (e.g. the public vehicle-delete flow).
	Vehicle loginVehicle(String userName, String password, Double lat, Double lng);

	Vehicle registerVehicle(Vehicle vehicle);

	Vehicle deactivateVehicle(Long vehicleId);

	Vehicle deactivateVehicleForVendor(Long vehicleId, Long vendorId);

	Vehicle activateVehicle(Long vehicleId);

	Vehicle activateVehicleForVendor(Long vehicleId, Long vendorId);

}
