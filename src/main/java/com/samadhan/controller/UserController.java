package com.samadhan.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Ride;
import com.samadhan.service.RidesService;

@RestController("/user")
public class UserController {
	
//@PostMapping("/findToeandServiceCenter")
//public void getAlltoeserviceandServiceCenter(@RequestParam Long latitude,@RequestParam Long longitude){
//	
//	
//	
//}
	
	@Autowired
	RidesService ridesService;
	
	
	@GetMapping(value = "/details/{userId}")
    public ResponseEntity<List<Ride>> getRidesByuser(@PathVariable Long userId) {
		List<Ride> ridesByUser = ridesService.getRidesByuser(userId);
		return ResponseEntity.ok(ridesByUser);
    }
	

}
