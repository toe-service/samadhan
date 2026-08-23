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

	Vehicle updateLocation(String address, Long vehicleId);

	Vehicle loginVehicle(String userName, String password);

	Vehicle registerVehicle(Vehicle vehicle);

}
