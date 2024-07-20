package com.samadhan.controller;

import java.util.List;

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
	    public ServiceCentre getAllServiceCentre() {
		 ServiceCentre resp = ServiceCentreService.getAllServiceCentres();
	       return resp;
	    }
	 
	 
	 @GetMapping(value = "/getAllServiceCentreByfilters")
//	    public List<ServiceCentre> getAllServiceCentreByfilters(@RequestParam String city,@RequestParam String pickuplatitude,@RequestParam String pickuplongitude,@RequestParam Long destinationlatitude,@RequestParam Long destinationlongitude, @RequestParam serviceTypeEnum serviceType) {
		public List<ServiceCentre> getAllServiceCentreByfilters(@RequestParam String city,
				@RequestParam String pickuplatitude, @RequestParam String pickuplongitude) {
			
		 	List<ServiceCentre> serviceCentresWithinFiftyKm = ServiceCentreService.getAllServiceCentreByfilters(city,
					pickuplatitude, pickuplongitude);
			return serviceCentresWithinFiftyKm;
		}
	 
	 
	 @PostMapping(value = "/addServiceCentre")
	    public ServiceCentre createServiceCentre(@RequestBody ServiceCentre serviceCentre) {
		 ServiceCentre resp = ServiceCentreService.createServiceCentre(serviceCentre);
	       return resp;
	    }
	

}
