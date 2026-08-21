package com.samadhan.service;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.exception.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.client.util.Objects;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.exception.SamadhanException;
import com.samadhan.enums.UserRole;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.RidesRepository;
import com.samadhan.repository.UserRepository;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.entity.RefreshToken;
import com.samadhan.response.LoginResponse;
import com.samadhan.security.RefreshTokenService;
import com.samadhan.security.TokenApi;
import com.samadhan.util.FireBaseMessagingService;

@Service
public class driversServiceImpl implements driversService {
	
 		

    @Autowired
    DriverRepository driverRepo;

    @Autowired
    TokenApi tokenApi;

    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RidesRepository rideRepo;

    @Autowired
    UserRepository userRepo;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    
    @Autowired
	VehicleRepository vehicleRepo;

    @Override
    public Driver getById(Long id) {

        Optional<Driver> optdriver = driverRepo.findById(id);

        Driver driver = optdriver.get();

        return driver;
    }


    @Override
    public Ride getRideByRideId(Long rideId) throws NotFoundException {

        return rideRepo.findById(rideId)
                .orElseThrow(() -> new NotFoundException("ride not found with id " + rideId));

    }

    @Override
    public Ride getdriverResponse(long driverid, long userId, String rideId, String destinationLatitude, String destinationLongitude, String pickupLatitude, String pickupLongitude) throws Exception {
        try {
            Ride ride = new Ride();

            //Optional<User> user=userRepo.findById(userId);
            UserDetails userDetails = userRepo.findById(userId)
                    .orElseThrow(() -> new SamadhanException("User with id " + userId + " not found"));
            Optional<Driver> driver=driverRepo.findById(driverid);
            
        	int generatedotp = SECURE_RANDOM.nextInt(1_000_0);
    		
    		

            Ride ridepresent = rideRepo.existRide(rideId, userId);
            System.out.println("ridepresent"+ridepresent);
            if (ridepresent != null) {
                throw new SamadhanException("Ride is Already Accepted");
            }


            //ride.setDriver(driver);
            ride.setRideStatus(1);
            ride.setDriverResponse(true);
            ride.setDriverDeclinationReason("NA");
            ride.setRideOtp(generatedotp);
            ride.setUser(userDetails);
            ride.setRideResponseTime(LocalDateTime.now());
            ride.setRideId(rideId);
            ride.setDriverName(driver.get().getDriverName());
            ride.setCarNumber(driver.get().getCarNumber());
            ride.setDestinationLatitude(destinationLatitude);
            ride.setDestinationLongitude(destinationLongitude);
            ride.setSourceLatitude(pickupLongitude);
            ride.setSourceLongitude(pickupLongitude);
            rideRepo.save(ride);

            System.out.println();

            return ride;

        } catch (SamadhanException e) {
            throw e;
        }

    }

    @Override
    public List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude) {
        double distance = 50.0;
        //find service centre within 50km

        List<Driver> driversWithinFiftyKm = driverRepo.findAllDriversByfilters(pickuplatitude, pickuplongitude, distance);

        return driversWithinFiftyKm;
    }

    @Override
    public Driver createdriver(Driver driver) {
    	System.out.println("Inside driver"+driver);
    	String name=driver.getDriverName();
    	String contactNumber=driver.getDriverContactNumber();
    	
    	String password = name.substring(0, Math.min(3, name.length())) +
                contactNumber.substring(Math.max(0, contactNumber.length() - 5));
    	
    	driver.setPassword(password);
        Driver driverData = driverRepo.save(driver);
        return driverData;
    }


	@Override
	public Ride cancelRide(long userId, String rideId, String reason) {
		 //Ride ride = new Ride();
		 Ride ridepresent = rideRepo.existRide(rideId, userId);
		 int rideStatus=rideStatusEnum.CANCELLED.getId();
		 ridepresent.setRideStatus(rideStatus);
		// ridepresent.setRideId(rideId);
		 ridepresent.setDriverDeclinationReason(reason);
		 rideRepo.save(ridepresent);
		
		return ridepresent;
	}

    @Override
    public void updateDriverLatLong(long driverId, String latitude, String longitude) throws NotFoundException {
        if(!driverRepo.existsById(driverId)) {
            throw new NotFoundException("driver with this driverId [%s] not exists".formatted(driverId));
        }

        Optional<Driver> optionalDriver = driverRepo.findById(driverId);
        Driver updatedDriver = optionalDriver.map(driver -> {
            driver.setDriverLatitude(latitude);
            driver.setDriverLongitude(longitude);
            return driver;
        }).get();

        driverRepo.save(updatedDriver);
    }


	@Override
	public List<Driver> getAllDrivers() {
		List<Driver> drivers = driverRepo.findAll();
		return drivers;
	}


	@Override
	public List<Driver> getAllDriversByVendor(Long vendorId) {
		List<Driver> drivers = driverRepo.findByVendorId(vendorId);
		return drivers;
	}


	@Override
	public Driver loginDriver(String userName, String password) {

		Driver driver = driverRepo.findByUserNamePassword(userName,password);
		
		return driver;
	}


	@Override
	public LoginResponse loginRole(String username, String password, String fcmToken) {

	    // 🔹 Check Driver
	    Driver driver = driverRepo.findByUserNamePassword(username, password);

	    if (driver != null) {
	        LoginResponse res = new LoginResponse();
	        res.setUsername(driver.getDriverEmail());
	        res.setDriverId(driver.getId());
	        res.setUserType("driver");
	        String token = tokenApi.generateToken(
	                driver.getDriverEmail(), UserRole.DRIVER.getValue(), driver.getId(), 15);
	        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
	                driver.getDriverEmail(), UserRole.DRIVER.getValue(), driver.getId());
	        res.setToken(token);
	        res.setRefreshToken(refreshToken.getToken());
	        res.setExpiresIn(900000L);
	        return res;
	    }

	    // 🔹 Check Vehicle
	    Vehicle vehicle = vehicleRepo.findByUserNamePassword(username, password);

	    if (vehicle != null) {
	        LoginResponse res = new LoginResponse();
	        res.setUsername(vehicle.getUserName());
	        res.setVehicleId(vehicle.getId());
	        res.setUserType("vehicle");
	       vehicle.setFcmToken(fcmToken);
	       res.setVendorId(vehicle.getTransferVendor().getId());
	        vehicleRepo.save(vehicle);
	        String token = tokenApi.generateToken(
	                vehicle.getUserName(), UserRole.VEHICLE.getValue(), vehicle.getId(), 15);
	        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
	                vehicle.getUserName(), UserRole.VEHICLE.getValue(), vehicle.getId());
	        res.setToken(token);
	        res.setRefreshToken(refreshToken.getToken());
	        res.setExpiresIn(900000L);
	        return res;
	    }

	    // ❌ Not found
	    throw new RuntimeException("Invalid credentials");
	}


	@Override
	public Driver updateLocation(String address, Long id) {
		 Driver driver = driverRepo.findById(id)
		            .orElseThrow(() -> new RuntimeException("Driver not found with id: " + id));
		 driver.setCurrentLocation(address);
		 driverRepo.save(driver);
		return driver;
	}


	@Override
	public Driver deleteDriver(Long driverId) {
		 Driver driver = driverRepo.findById(driverId)
		            .orElseThrow(() -> new RuntimeException("Driver not found with id: " + driverId));
		 driverRepo.delete(driver);
		return driver;
	}

}
