package com.samadhan.service;
import java.util.List;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.entity.Vehicle;
import com.samadhan.exception.NotFoundException;
import com.samadhan.response.LoginResponse;


public interface driversService {

	Driver getById(Long id);

	Ride getdriverResponse(long driverid,long userId,String rideId, String destinationLatitude, String destinationLongitude, String pickupLatitude, String pickupLongitude) throws Exception;

	List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude);

	Driver createdriver(Driver driver);

    Ride getRideByRideId(Long rideId) throws NotFoundException;

	Ride cancelRide(long userId, String rideId, String reason);

	void updateDriverLatLong(long driverId, String latitude, String longitude) throws NotFoundException;

	List<Driver> getAllDrivers();

	List<Driver> getAllDriversByVendor(Long vendorId);

	Driver loginDriver(String userName, String password);

	LoginResponse loginRole(String userName, String password, String fcmToken);

	Driver updateLocation(String address, Long id);

	Driver deleteDriver(Long driverId);

}
