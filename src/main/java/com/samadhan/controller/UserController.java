package com.samadhan.controller;

import java.util.List;

import com.samadhan.entity.User;
import com.samadhan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.samadhan.entity.Ride;
import com.samadhan.service.RidesService;

@RestController
@RequestMapping("/user")
public class UserController {
	
//@PostMapping("/findToeandServiceCenter")
//public void getAlltoeserviceandServiceCenter(@RequestParam Long latitude,@RequestParam Long longitude){
//	
//	
//	
//}
	
	@Autowired
	RidesService ridesService;

	@Autowired
	UserService userservice;
	
	
	@GetMapping(value = "/ridedetails/{userId}")
    public ResponseEntity<List<Ride>> getRidesByuser(@PathVariable Long userId) {
		System.out.println("hi");
		System.out.println("userId" +userId);
		List<Ride> ridesByUser = ridesService.getRidesByuser(userId);
		return ResponseEntity.ok(ridesByUser);
    }


	@GetMapping(value = "/userdetails/{userId}")
	public ResponseEntity<User> getuserdetails(@PathVariable Long userId) {
		System.out.println("hi");
		System.out.println("userId" +userId);
		User ridesByUser = userservice.findById(userId);
		return ResponseEntity.ok(ridesByUser);
	}

}
