package com.samadhan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.service.TransferVendorService;
import com.samadhan.service.VehicleService;

@RestController
@RequestMapping(value = "/transferVendor")
public class TransferVendorController {

	 @Autowired
	 TransferVendorService transferVendorService;
	 
	@PostMapping(value = "/register-vendor")
	public TransferVendor registerVehicle(@RequestBody TransferVendor transferVendor) {
		
		TransferVendor resp = transferVendorService.registerVendor(transferVendor);
		return resp;
	}
	
}
