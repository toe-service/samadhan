package com.samadhan.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.samadhan.dto.NotificationMessage;
import com.samadhan.dto.ServiceCentreWrapper;
import com.samadhan.entity.Driver;
import com.samadhan.entity.ServiceCentre;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.repository.VehicleRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class FireBaseMessagingService {

	@Autowired
	FirebaseMessaging firebaseMessaging;
	
	

	public String sendNotificationByToken(NotificationMessage notificationMessage) {

		List<ServiceCentreWrapper> driverList=notificationMessage.getDrivers();

		try {

			for(ServiceCentreWrapper driver: driverList) {

			Notification notification = Notification
					.builder()
					.setTitle(notificationMessage.getTitle())
					.setBody(notificationMessage.getBody())
					.setImage(notificationMessage.getImage())
					.build();
			//Message message = Message.builder().setToken(notificationMessage.getRecipientToken()).setNotification(notification).putAllData(notificationMessage.getData()).build();
			Message message = Message
					.builder()
					.setToken(driver.getDriverToken())
					.setNotification(notification)
					.putAllData(notificationMessage.getData())
					.build();

			firebaseMessaging.send(message);


			}

		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
			return "Error Sending Notification";
		}
		return "Success Sending Notification";
	}
	
	
	@Autowired
	private VehicleRepository vehicleRepository;



	public void notifyVehicles(TransferRequestDetails request) throws FirebaseMessagingException {

	    List<Vehicle> vehicles =
	            vehicleRepository.findNearbyVehicles(
	                    request.getVendorPickupVehicle().name(),
	                    request.getSourceLatitude(),
	                    request.getSourceLongitude());

	    for (Vehicle vehicle : vehicles) {

	        if (vehicle.getFcmToken() == null || vehicle.getFcmToken().isEmpty()) {
	            continue;
	        }

//	        firebaseService.sendPushNotification(
//	                vehicle.getFcmToken(),
//	                "New Booking Available",
//	                request.getSource() + " → " + request.getDestination(),
//	                request.getId().toString());

			Notification notification = Notification
					.builder()
					.setTitle("New Booking Available")
				    .setBody(request.getSource() + " → " + request.getDestination())
				//	.setImage(notificationMessage.getImage())
					.build();
			//Message message = Message.builder().setToken(notificationMessage.getRecipientToken()).setNotification(notification).putAllData(notificationMessage.getData()).build();
			Message message = Message
					.builder()
					.setToken(vehicle.getFcmToken())
					.setNotification(notification)
				    .putData("requestId", request.getId().toString())
	                .putData("serviceType", request.getServiceType().name())
	                .putData("source", request.getSource())
                    .putData("destination", request.getDestination())
					.build();

			 firebaseMessaging.send(message);
	    }
	}
	
}
