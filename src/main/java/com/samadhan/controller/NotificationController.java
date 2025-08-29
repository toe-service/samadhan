package com.samadhan.controller;
import com.samadhan.dto.ServiceCentreWrapper;
import com.samadhan.util.FireBaseMessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.samadhan.dto.NotificationMessage;

import java.util.List;

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