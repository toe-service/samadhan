package com.samadhan.service;


import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.samadhan.entity.UserDetails;
import com.samadhan.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.exception.SamadhanException;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.RidesRepository;
import com.samadhan.repository.UserRepository;

@Service
public class driversServiceImpl implements driversService {

    @Autowired
    DriverRepository driverRepo;

    @Autowired
    RidesRepository rideRepo;

    @Autowired
    UserRepository userRepo;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

}
