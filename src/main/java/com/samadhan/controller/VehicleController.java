package com.samadhan.controller;

import java.util.Map;

import javax.websocket.server.PathParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Vehicle;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.service.LocationService;
import com.samadhan.service.VehicleService;
import com.samadhan.service.driversService;

@RestController
@RequestMapping(value = "/vehicle")
public class VehicleController {
	
	  private final LocationService locationService;
	  
	  public VehicleController(LocationService service){
	        this.locationService = service;
	    }

	  @Autowired
	  VehicleService vehicleService;
	  
	  @Autowired
	  driversService driverService;
	
	@PatchMapping(value = "/update-roleLocation/{Id}")
	public String  updateRoleLocation(@PathVariable Long Id, @RequestParam double lat,
            @RequestParam double lng, @RequestParam String userType) {
		
		Map<String, Object>  resp = locationService.getLocation(lat,lng);
		
		String address = (String) resp.get("address");
		
		if(userType.equalsIgnoreCase("vehicle")) {
		Vehicle vehicle=vehicleService.updateLocation(address,Id);
		return "Vehicle Location Updated";
		}else {
		Driver driver=driverService.updateLocation(address,Id);	
		return "Driver Location Updated";
		}
		
	}
	
	@PostMapping(value = "/register-Vehicle")
	public Vehicle registerVehicle(@RequestBody Vehicle vehicle) {
		
		Vehicle resp = vehicleService.registerVehicle(vehicle);
		return resp;
	}
	

}
