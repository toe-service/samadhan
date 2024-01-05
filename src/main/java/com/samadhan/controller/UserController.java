package com.samadhan.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/user")
public class UserController {
	
@PostMapping("/findToeandServiceCenter")
public void getAlltoeserviceandServiceCenter(@RequestParam Long latitude,@RequestParam Long longitude){
	
	
	
}
	

}
