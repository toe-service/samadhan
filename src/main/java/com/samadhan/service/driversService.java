package com.samadhan.service;
import java.util.List;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.exception.NotFoundException;


public interface driversService {

	Driver getById(Long id);

	Ride getdriverResponse(long driverid,long userId,String rideId, String destinationLatitude, String destinationLongitude, String pickupLatitude, String pickupLongitude) throws Exception;

	List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude);

	Driver createdriver(Driver driver);

    Ride getRideByRideId(Long rideId) throws NotFoundException;

	Ride cancelRide(long userId, String rideId, String reason);

}
