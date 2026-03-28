package com.samadhan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Vehicle;
import com.samadhan.repository.VehicleRepository;

@Service
public class VehicleServiceImpl implements VehicleService{
	
	@Autowired
	VehicleRepository vehicleRepo;

	@Override
	public List<Vehicle> getAllVehiclesByVendor(Long vendorId) {

		List<Vehicle> vehicleByVendor=vehicleRepo.findByVendorId(vendorId);
		return vehicleByVendor;
	}

	@Override
	public Vehicle createVehicle(Vehicle vehicle) {

		Vehicle vehicleregister=vehicleRepo.save(vehicle);
		return vehicleregister;
	}

}
