package com.samadhan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.service.RidesService;

@RestController("/ride")
public class RidesController {
	
	@Autowired
	RidesService ridesService;
	
	@PostMapping(value = "/pop-remove")
    public ResponseEntity<Ride> popupRemove(@RequestBody Ride ride) {
		Ride response = ridesService.popupRemove(ride);
		return ResponseEntity.ok(response);
    }
	

}
