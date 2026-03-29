package com.samadhan.service;

import java.util.List;
import java.util.Map;

import com.samadhan.entity.Vehicle;

public interface VehicleService {

	List<Vehicle> getAllVehiclesByVendor(Long vendorId);

	Vehicle createVehicle(Vehicle vehicle);

	Vehicle updateLocation(String address, Long vehicleId);

}
