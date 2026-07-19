package com.samadhan.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Vehicle;
import com.samadhan.repository.VehicleRepository;

@Service
public class VehicleServiceImpl implements VehicleService{
	
	@Autowired
	VehicleRepository vehicleRepo;

	@Override
	public List<Vehicle> getAllVehiclesByVendor(Long vendorId, boolean isActive) {
		List<Vehicle> vehicleByVendor=new ArrayList<>();
		if(isActive) {
		boolean isOngoing=false;
		vehicleByVendor=vehicleRepo.findByActiveVendorId(vendorId, isOngoing);
		}else {
		vehicleByVendor=vehicleRepo.findByVendorId(vendorId);
		}
		return vehicleByVendor;
	}

	@Override
	public Vehicle createVehicle(Vehicle vehicle) {
		
		if (vehicle.getVehicleNumber() != null) {
		String userName=vehicle.getVehicleNumber().replaceAll("\\s+", "")+ "@gmail.com";
		
		String password = vehicle.getVehicleNumber() == null ? "" :
			vehicle.getVehicleNumber().replaceAll("\\s+", "")
		                 .substring(Math.max(0, vehicle.getVehicleNumber().replaceAll("\\s+", "").length() - 6));
		
		vehicle.setUserName(userName);
		vehicle.setPassword(password);
		}
		
		if (vehicle.getVehicleContactNumber() != null) {
			vehicle.setVehicleContactNumber(vehicle.getVehicleContactNumber());
		}
		
		vehicle.setVendorVehicle(vehicle.getVendorVehicle());
		vehicle.setVehicleCategory(vehicle.getVehicleCategory());
		
		Vehicle vehicleregister=vehicleRepo.save(vehicle);
		return vehicleregister;
	}

	@Override
	public Vehicle updateLocation(String address, Long vehicleId) {
		
		Vehicle vehicle = vehicleRepo.findById(vehicleId)
		            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + vehicleId));
		vehicle.setCurrentLocation(address);
		vehicleRepo.save(vehicle);
		return vehicle;
	}

	@Override
	public Vehicle loginVehicle(String userName, String password) {
		Vehicle vehicle=vehicleRepo.findByUserNamePassword(userName, password);
		return vehicle;
	}

	@Override
	public Vehicle registerVehicle(Vehicle vehicle) {
		
		return null;
	}

}
