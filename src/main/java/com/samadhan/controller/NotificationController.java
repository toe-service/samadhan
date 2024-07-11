package com.samadhan.controller;
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.web.bind.annotation.PostMapping; 
import org.springframework.web.bind.annotation.RequestBody; 
import org.springframework.web.bind.annotation.RequestMapping; 
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.dto.NotificationMessage;
import com.samadhan.util.FireBaseMessagingService; 

@RestController 
@RequestMapping("/notification") 
public class NotificationController {

    @Autowired 
    FireBaseMessagingService fireBaseMessagingService; 
	
	@PostMapping("/notify") 
	public String sendNotificationByToken(@RequestBody NotificationMessage notificationMessage){ 
		return fireBaseMessagingService.sendNotificationByToken(notificationMessage); 
	} 
	
	
}