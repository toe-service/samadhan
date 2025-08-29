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

//import com.kent.smartassist.constant.SmartAssistanceConstant;
//import com.kent.smartassist.exception.SmartAssistanceException;
//import com.kent.smartassist.reponse.util.ResponseUtil;
import com.samadhan.response.*;
import com.samadhan.response.Error;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.entity.ServiceCentre;
import com.samadhan.exception.SamadhanException;
import com.samadhan.service.driversService;
import com.samadhan.util.ResponseUtil;



@RestController
@RequestMapping(value = "/driver")
public class DriverController {

	@Autowired
	private driversService driversService;
	
	@GetMapping(value = "/driver-details")
    public ResponseEntity<Driver> driverDetailsById(@RequestParam Long id) {
		Driver response = driversService.getById(id);
		//hhjjhj
		return ResponseEntity.ok(response);
        
    }
	
	@PostMapping(value = "/driver-response")
    public ResponseEntity<Object> driverResponse(@RequestParam String rideId,@RequestBody Driver driver,@RequestParam int otp,@RequestParam long userId) throws Exception {
	try {
		Ride response = driversService.getdriverResponse(driver,otp,userId,rideId);
	//	return ResponseEntity.ok(response);
		
		
		return ResponseEntity
				.ok(ResponseUtil.populateResponseObject(response, "SUCCESS", null));
	} catch (SamadhanException ex) {
		//log.error("Inside catch block of  DeviceController getAllDevices() method::: " + ex.getMessage(), ex);
		return ResponseEntity.ok(ResponseUtil.populateResponseObject(null, "FAIL",
				new Error("Driver",ex.getMessage())));
	}
		
		
        
    }
	
	
	 @GetMapping(value = "/getAllDriversByfilters")
//	    public List<ServiceCentre> getAllServiceCentreByfilters(@RequestParam String city,@RequestParam String pickuplatitude,@RequestParam String pickuplongitude,@RequestParam Long destinationlatitude,@RequestParam Long destinationlongitude, @RequestParam serviceTypeEnum serviceType) {
		public List<Driver> getAllDriversByfilters(
				@RequestParam String pickuplatitude, @RequestParam String pickuplongitude) {
			
		 	List<Driver> driversWithinFiftyKm = driversService.getAllDriversByfilters(pickuplatitude, pickuplongitude);
			return driversWithinFiftyKm;
		}
	
	//Driver registration API
	 
	 @PostMapping(value = "/register-Driver")
	    public Driver createDriver(@RequestBody Driver driver) {
		 Driver resp = driversService.createdriver(driver);
	       return resp;
	    }

}
