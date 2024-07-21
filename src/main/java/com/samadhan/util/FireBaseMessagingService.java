package com.samadhan.util;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.samadhan.dto.NotificationMessage;
import com.samadhan.entity.Driver;
import com.samadhan.entity.ServiceCentre;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FireBaseMessagingService {

	@Autowired
	FirebaseMessaging firebaseMessaging;

	public String sendNotificationByToken(NotificationMessage notificationMessage) {
		
		List<Driver> driverList=notificationMessage.getDrivers();
		
		try {
		
			for(Driver driver: driverList) {
			
			Notification notification = Notification.builder().setTitle(notificationMessage.getTitle()).setBody(notificationMessage.getBody()).setImage(notificationMessage.getImage()).build();
			Message message = Message.builder().setToken(notificationMessage.getRecipientToken()).setNotification(notification).putAllData(notificationMessage.getData()).build();
			//Message message = Message.builder().setToken(driver.getDriverToken()).setNotification(notification).putAllData(notificationMessage.getData()).build();
		
			firebaseMessaging.send(message);
			
			
			}
			
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
			return "Error Sending Notification";
		}
		return "Success Sending Notification";
	}
}
