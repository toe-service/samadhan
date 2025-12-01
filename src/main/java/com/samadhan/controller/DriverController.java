package com.samadhan.controller;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.samadhan.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
		Object populateResponseObject = ResponseUtil.populateResponseObject(response, "SUCCESS", null);

		return ResponseEntity
				.ok(populateResponseObject);
	} catch (SamadhanException ex) {
		//log.error("Inside catch block of  DeviceController getAllDevices() method::: " + ex.getMessage(), ex);
		throw ex;
	}
	catch (Exception exp) {
		throw exp;
	}
		
		
        
    }

    @GetMapping(value = "/getRideByRideId/{rideId}")
    public ResponseEntity<Ride> getRideByRideId(@PathVariable("rideId") Long rideId) throws NotFoundException {
        Ride ride =  driversService.getRideByRideId(rideId);
        return ResponseEntity.ok(ride);
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
