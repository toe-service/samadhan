package com.samadhan.service;
import java.util.List;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;


public interface driversService {

	Driver getById(Long id);

	Ride getdriverResponse(Driver driver,int otp,long userId,String rideId) throws Exception;

	List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude);

	Driver createdriver(Driver driver);

}
