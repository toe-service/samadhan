package com.samadhan.controller;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.samadhan.dto.ServiceCentreWrapper;
import com.samadhan.entity.Driver;
import com.samadhan.entity.VehicleTransfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.ServiceCentre;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.service.ServiceCentreService;

@RestController
@RequestMapping(value = "/service-centre-tow")
public class ServiceCentreAndTowController {

	@Autowired
	ServiceCentreService ServiceCentreService;
	
	
	 @GetMapping(value = "/getAllServiceCentre")
	    public List<ServiceCentre> getAllServiceCentre() {
		 System.out.println("hi");
		 List<ServiceCentre> resp = ServiceCentreService.getAllServiceCentres();
		 System.out.println("resp"+resp);
	       return resp;
	    }
	 
	 
	 @GetMapping(value = "/getAllServiceCentreByfilters")
//	    public List<ServiceCentre> getAllServiceCentreByfilters(@RequestParam String city,@RequestParam String pickuplatitude,@RequestParam String pickuplongitude,@RequestParam Long destinationlatitude,@RequestParam Long destinationlongitude, @RequestParam serviceTypeEnum serviceType) {
		public List<ServiceCentre> getAllServiceCentreByfilters(@RequestParam Integer serviceType,
				@RequestParam String pickuplatitude, @RequestParam String pickuplongitude,@RequestParam String destinationlatitude,@RequestParam String destinationlongitude) {
		 System.out.println("hi");
		 	List<ServiceCentre> serviceCentresWithinFiftyKm = ServiceCentreService.getAllServiceCentreByfilters(serviceType,
					pickuplatitude, pickuplongitude,destinationlatitude, destinationlongitude);
			return serviceCentresWithinFiftyKm;
		}

	@GetMapping(value = "/getAllServiceCentreDriverByfilters")
	public List<ServiceCentreWrapper> getAllServiceCentreDriverByfilters (@RequestParam Integer serviceType,
															@RequestParam String pickuplatitude, @RequestParam String pickuplongitude,@RequestParam String destinationlatitude,@RequestParam String destinationlongitude) throws JsonProcessingException {
		System.out.println("hi");
		List<ServiceCentreWrapper> serviceCentresDriversWithinFiftyKm = ServiceCentreService.getAllServiceCentreDriverByfilters(serviceType,
				pickuplatitude, pickuplongitude,destinationlatitude, destinationlongitude);
		return serviceCentresDriversWithinFiftyKm;
	}

	@PostMapping(value = "/requestRideTransfer")
	public VehicleTransfer requestRideTransfer(@RequestParam String vehicleType,@RequestParam String VehicleModel,@RequestParam Date requestDate,@RequestParam String city,
																		  @RequestParam String pickuplatitude, @RequestParam String pickuplongitude,@RequestParam String destinationlatitude,@RequestParam String destinationlongitude) throws JsonProcessingException {
		System.out.println("hi");
		VehicleTransfer rideTransfer = ServiceCentreService.requestRideTransfer(vehicleType,VehicleModel,requestDate,city,
				pickuplatitude, pickuplongitude,destinationlatitude, destinationlongitude);
		return rideTransfer;
	}
	 
	 
	 @PostMapping(value = "/addServiceCentre")
	    public ServiceCentre createServiceCentre(@RequestBody ServiceCentre serviceCentre) {
		 ServiceCentre resp = ServiceCentreService.createServiceCentre(serviceCentre);
	       return resp;
	    }
	

}
