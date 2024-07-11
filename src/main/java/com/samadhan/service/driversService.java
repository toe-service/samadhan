package com.samadhan.service;
import com.samadhan.entity.Driver;


public interface driversService {

	Driver getById(Long id);

	Driver getdriverResponse(Driver driver);

}
