package com.samadhan.service;

import java.util.List;
import java.util.Map;

import com.samadhan.entity.Vehicle;

public interface VehicleService {

	List<Vehicle> getAllVehiclesByVendor(Long vendorId, boolean isActive);

	Vehicle createVehicle(Vehicle vehicle);

	Vehicle updateLocation(String address, Long vehicleId);

	Vehicle loginVehicle(String userName, String password);

	Vehicle registerVehicle(Vehicle vehicle);

}
