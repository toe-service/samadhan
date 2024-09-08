package com.samadhan.service;
import java.util.List;

import com.samadhan.entity.Driver;


public interface driversService {

	Driver getById(Long id);

	Driver getdriverResponse(Driver driver,int otp,long userId);

	List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude);

	Driver createdriver(Driver driver);

}
