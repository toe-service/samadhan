package com.samadhan.service;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.type.DateTime;

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
	public Ride getdriverResponse(Driver driver, int otp,long userId,String rideId) throws Exception {
		try {
			Ride ride = new Ride();

			//Optional<User> user=userRepo.findById(userId);
			User user = userRepo.findById(userId)
					.orElseThrow(() -> new SamadhanException("User with id " + userId + " not found"));


			Ride ridepresent = rideRepo.existRide(driver.getId(), userId);

			if (ridepresent != null) {
				throw new SamadhanException("Ride is Already Accepted");
			}


			ride.setDriver(driver);
			ride.setRideStatus(true);
			ride.setDriverResponse(true);
			ride.setDriverDeclinationReason("NA");
			ride.setRideOtp(otp);
			ride.setUser(user);
			ride.setRideResponseTime(LocalDateTime.now());
			ride.setRideId(rideId);
			rideRepo.save(ride);

			System.out.println();

			return ride;

		}catch (SamadhanException e){
			throw e;
		}

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
