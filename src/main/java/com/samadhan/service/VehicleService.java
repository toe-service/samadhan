package com.samadhan.service;

import java.util.List;

import com.samadhan.entity.Vehicle;

public interface VehicleService {

	List<Vehicle> getAllVehiclesByVendor(Long vendorId);

	Vehicle createVehicle(Vehicle vehicle);

}
