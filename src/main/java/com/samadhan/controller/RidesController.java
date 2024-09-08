package com.samadhan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.service.RidesService;

@RestController
@RequestMapping(value = "/ride")
public class RidesController {
	
	@Autowired
	RidesService ridesService;
	
	@GetMapping(value = "/pop-remove")
    public ResponseEntity<Ride> popupRemove(@RequestParam Long rideId,@RequestParam Long userId,@RequestParam Long driverId) {
		Ride response = ridesService.popupRemove(rideId,userId,driverId);
		return ResponseEntity.ok(response);
    }
	
	
	@GetMapping(value = "/ride/{userId}")
    public ResponseEntity<List<Ride>> getRidesByuser(@PathVariable Long userId) {
		List<Ride> ridesByUser = ridesService.getRidesByuser(userId);
		return ResponseEntity.ok(ridesByUser);
    }
	

}
