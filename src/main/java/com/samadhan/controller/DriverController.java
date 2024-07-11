package com.samadhan.controller;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.samadhan.entity.Driver;
import com.samadhan.service.driversService;



@RestController
@RequestMapping(value = "/driver")
public class DriverController {

	@Autowired
	private driversService driversService;
	
	@GetMapping(value = "/driver-details")
    public ResponseEntity<Driver> driverDetailsById(@RequestParam Long id) {
		Driver response = driversService.getById(id);
		return ResponseEntity.ok(response);
        
    }
	
	@PostMapping(value = "/driver-response")
    public ResponseEntity<Driver> driverResponse(@RequestBody Driver driver) {
		Driver response = driversService.getdriverResponse(driver);
		return ResponseEntity.ok(response);
        
    }

}
