package com.samadhan.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.entity.ServiceCentre;
import com.samadhan.entity.User;
import com.samadhan.exception.SamadhanException;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.RidesRepository;
import com.samadhan.repository.ServiceCentreRepo;
import com.samadhan.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class driversServiceImpl implements driversService{

	@Autowired
	DriverRepository driverRepo;
	
	@Autowired
	RidesRepository rideRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Override
	public Driver getById(Long id) {
		
		Optional<Driver> optdriver=driverRepo.findById(id);
		
		Driver driver=optdriver.get();
		
		return driver;
	}

	@Override
	public Ride getdriverResponse(Driver driver, int otp,long userId) throws Exception {
		
		Ride ride=new Ride();
		
		Optional<User> user=userRepo.findById(userId);
		
		Ride ridepresent=rideRepo.existRide(driver.getId(),userId);
		
		if(ridepresent!=null) {
			throw new SamadhanException("Ride Already Exist");
		}
		
		
		
		ride.setDriver(driver);
		ride.setRideStatus(true);
		ride.setDriverResponse(true);
		ride.setDriverDeclinationReason("NA");
		ride.setRideOtp(otp);
		ride.setUser(user.get());
		ride.setRideResponseTime(LocalDateTime.now());
		
		rideRepo.save(ride);
		
		System.out.println();
		
		return ride;
	}

	@Override
	public List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude) {
		double distance=50.0;
		//find service centre within 50km
		
		List<Driver> driversWithinFiftyKm=driverRepo.findAllDriversByfilters(pickuplatitude,pickuplongitude,distance);
		
		return driversWithinFiftyKm;
	}

	@Override
	public Driver createdriver(Driver driver) {
		
		Driver driverData=driverRepo.save(driver);
		return driverData;
	}

}
