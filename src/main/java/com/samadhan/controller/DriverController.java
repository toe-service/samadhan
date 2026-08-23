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
import org.springframework.web.multipart.MultipartFile;

//import com.kent.smartassist.constant.SmartAssistanceConstant;
//import com.kent.smartassist.exception.SmartAssistanceException;
//import com.kent.smartassist.reponse.util.ResponseUtil;
import com.samadhan.response.*;
import com.samadhan.response.Error;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.entity.ServiceCentre;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.exception.SamadhanException;
import com.samadhan.service.VehicleService;
import com.samadhan.service.driversService;
import com.samadhan.util.ResponseUtil;



@RestController
@RequestMapping(value = "/driver")
public class DriverController {

	@Autowired
	private driversService driversService;
	
	@Autowired
	private VehicleService vehicleService;
	
	@GetMapping(value = "/driver-details")
    public ResponseEntity<Driver> driverDetailsById(@RequestParam Long id) {
		Driver response = driversService.getById(id);
		//hhjjhj
		return ResponseEntity.ok(response);
        
    }


	@PostMapping(value = "/driver-response")
    public ResponseEntity<ResponseObject<Ride>> driverResponse(@RequestParam String rideId,
															   @RequestParam long driverId,
															   @RequestParam long userId,
															   @RequestParam String destinationLatitude,
															   @RequestParam String destinationLongitude,
															   @RequestParam String pickupLatitude,
															   @RequestParam String pickupLongitude) throws Exception {
	try {
		Ride response = driversService.getdriverResponse(driverId,userId,rideId,destinationLatitude,destinationLongitude,pickupLatitude,pickupLatitude);
	//	return ResponseEntity.ok(response);
		 ResponseObject<Ride> populateResponseObject = ResponseUtil.populateResponseObject(response, "SUCCESS", null);

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
	
	@PostMapping(value = "/cancel-ride")
    public ResponseEntity<Object> cancelRide(@RequestParam String rideId,
											 @RequestParam long userId,
											 @RequestParam String reason) throws Exception {
	try {
		Ride response = driversService.cancelRide(userId,rideId, reason);
		Object populateResponseObject = ResponseUtil.populateResponseObject(response, "SUCCESS", null);

		return ResponseEntity
				.ok(populateResponseObject);
	} catch (Exception ex) {
		throw ex;
	}
		
		
        
    }

    @GetMapping(value = "/getRideByRideId/{rideId}")
    public ResponseEntity<Ride> getRideByRideId(@PathVariable("rideId") Long rideId) throws NotFoundException {
        Ride ride =  driversService.getRideByRideId(rideId);
        return ResponseEntity.ok(ride);
    }
	
	
	 @GetMapping(value = "/getAllDriversByfilters")
//	    public List<ServiceCentre> getAllServiceCentreByfilters(@RequestParam String city,@RequestParam String pickuplatitude,@RequestParam String pickuplongitude,@RequestParam Long destinationlatitude,@RequestParam Long destinationlongitude, @RequestParam serviceTypeEnum serviceType) {
		public List<Driver> getAllDriversByfilters(@RequestParam String pickuplatitude,
												   @RequestParam String pickuplongitude) {
			
		 	List<Driver> driversWithinFiftyKm = driversService.getAllDriversByfilters(pickuplatitude, pickuplongitude);
			return driversWithinFiftyKm;
		}
	 
	 @GetMapping(value = "/getAllDrivers")
		public List<Driver> getAllDrivers() {
			
		 	List<Driver> drivers = driversService.getAllDrivers();
			return drivers;
		}
	 
	 @GetMapping(value = "/getAllDriversByVendor")
		public List<Driver> getAllDriversByVendor(@RequestParam Long vendorId) {
			
		 	List<Driver> drivers = driversService.getAllDriversByVendor(vendorId);
			return drivers;
		}
	 
	 @GetMapping(value = "/getAllVehiclesByVendor")
		public List<Vehicle> getAllVehiclesByVendor(@RequestParam Long vendorId,@RequestParam boolean isActive) {
			
		 	List<Vehicle> vehiclesByVendor = vehicleService.getAllVehiclesByVendor(vendorId, isActive);
			return vehiclesByVendor;
		}
	 
	
	//Driver registration API

	/**
	 * TODO: REMOVE TOKEN (done)
	 */
	@PostMapping(value = "/register-Driver")
	public Driver createDriver(@RequestBody Driver driver) {
		System.out.println("driver"+driver.getTransferVendor());
		Driver resp = driversService.createdriver(driver);
		return resp;
	}
	
	@PostMapping(value = "/register-Vehicle")
	public Vehicle createVehicle(@RequestParam String vehicleNumber,
			@RequestParam(required = false) String vehicleContactNumber,
			@RequestParam(required = false) String currentLocation,
			@RequestParam(required = false) VendorPickupVehicleEnum vendorVehicle,
			@RequestParam(required = false) VehicleCategoryEnum vehicleCategory,
			@RequestParam(required = false) String vehicleLatitude,
			@RequestParam(required = false) String vehicleLongitude,
			@RequestParam(required = false) String fcmToken,
			@RequestParam(required = false) Long transferVendorId,
			@RequestParam(required = false) MultipartFile rcFile) {

		Vehicle resp = vehicleService.createVehicle(vehicleNumber, vehicleContactNumber, currentLocation,
				vendorVehicle, vehicleCategory, vehicleLatitude, vehicleLongitude, fcmToken, transferVendorId,
				rcFile);
		return resp;
	}
	
	@DeleteMapping(value = "/delete-driver")
	public Driver deleteDriver(@RequestParam Long driverId) {
		
		Driver resp = driversService.deleteDriver(driverId);
		return resp;
	}



	@PostMapping("/update-lat-long/{driverId}")
	public ResponseEntity<ResponseObject<String>> updateLatLong(@PathVariable("driverId") Long driverId,
																@RequestParam("latitude") String latitude,
																@RequestParam("longitude")String longitude
	) throws NotFoundException {
		driversService.updateDriverLatLong(driverId, latitude, longitude);
		ResponseObject<String> success = ResponseUtil.populateResponseObject("User successfully updated", "SUCCESS", null);
		return ResponseEntity.ok(success);
	}

}
