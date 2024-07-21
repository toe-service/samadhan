package com.samadhan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Driver;
import com.samadhan.entity.ServiceCentre;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.ServiceCentreRepo;

import org.springframework.stereotype.Service;

@Service
public class driversServiceImpl implements driversService{

	@Autowired
	DriverRepository driverRepo;
	
	@Override
	public Driver getById(Long id) {
		//Driver d=new Driver();
		return null;
	}

	@Override
	public Driver getdriverResponse(Driver driver) {
		return null;
	}

	@Override
	public List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude) {
		double distance=50.0;
		//find service centre within 50km
		
		List<Driver> driversWithinFiftyKm=driverRepo.findAllDriversByfilters(pickuplatitude,pickuplongitude,distance);
		
		return driversWithinFiftyKm;
	}

}
