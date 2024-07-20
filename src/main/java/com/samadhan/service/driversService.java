package com.samadhan.service;
import java.util.List;

import com.samadhan.entity.Driver;


public interface driversService {

	Driver getById(Long id);

	Driver getdriverResponse(Driver driver);

	List<Driver> getAllDriversByfilters(String pickuplatitude, String pickuplongitude);

}
