package com.samadhan.util;

import java.util.Objects;

import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.firebase.messaging.AndroidConfig;
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
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.repository.VehicleRepository;

import lombok.extern.java.Log;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class FireBaseMessagingService {

	@Autowired
	FirebaseMessaging firebaseMessaging;
	
	   private static final Logger log = LoggerFactory.getLogger(FireBaseMessagingService.class);
	
	

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



	@Async("notificationExecutor")
	public void notifyVehicles(TransferRequestDetails request) throws FirebaseMessagingException {

		if (request.getSourceLatitude() == null || request.getSourceLongitude() == null  ) {
		    log.warn("Request {} has no pickup coordinates, cannot notify nearby vehicles", request.getId());
		    return;
		}
		
		if(request.getVendorPickupVehicle() == null && request.getUserType().equalsIgnoreCase("Vendor")) {
			  return;
		}

		// The requested vehicle type plus the next larger ones: a bigger vehicle standing
		// nearby can still take the load, so it is worth offering the ride to. A request
		// that names no vehicle type is offered to every nearby vehicle.
		VendorPickupVehicleEnum requiredVehicle = request.getVendorPickupVehicle();

		if (Objects.isNull(requiredVehicle)) {
		    log.warn("No vehicle found to notify for request {}", request.getId());
		    return;
		}

		List<Integer> vehicleTypes =
		        VendorPickupVehicleEnum.getRequestedAndLarger(requiredVehicle)
		                .stream()
		                .map(Enum::ordinal)
		                .collect(Collectors.toList());

		 List<Vehicle> vehicles =
		            vehicleRepository.findNearbyVehicles(
		                    requiredVehicle == null ? 1 : 0,
		                    vehicleTypes,
		                    request.getSourceLatitude(),
		                    request.getSourceLongitude(),
		                    request.getDistanceKm());

		 if (vehicles.isEmpty()) {
		     log.warn("No vehicle found to notify for request {}", request.getId());
		     return;
		 }

	    for (Vehicle vehicle : vehicles) {

	        if (vehicle.getFcmToken() == null || vehicle.getFcmToken().isEmpty()) {
	            log.warn("Vehicle {} has no FCM token, skipping notification for request {}", vehicle.getId(), request.getId());
	            continue;
	        }

			// Data-only message: no setNotification(...) block. This is required so the
			// driver app's setBackgroundMessageHandler runs even when the app is killed,
			// letting it display the full-screen "Accept / Decline" incoming-call UI via
			// Notifee. If a notification block is included, FCM's native SDK intercepts
			// it on killed-app state and only drops a tray notification (no JS runs).
			Message message = Message
					.builder()
					.setToken(vehicle.getFcmToken())
					.putData("type", "RIDE_REQUEST")
					.putData("rideCost",  String.valueOf(request.getRideCost()))
				    .putData("requestId", request.getId().toString())
	                .putData("serviceType", request.getServiceType().name())
	                .putData("source", request.getSource())
                    .putData("destination", request.getDestination())
                    .putData("title", "New Booking Available")
                    .putData("body", request.getSource() + " → " + request.getDestination())
	                .setAndroidConfig(
                    AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH) // wakes device even if backgrounded/killed
                            .build()
	                		)
					.build();
//	        Message message = Message
//	                .builder()
//	                .setToken(vehicle.getFcmToken())
//	                .putData("type", "RIDE_REQUEST")
//	                .putData("requestId", request.getId().toString())
//	                .putData("serviceType", request.getServiceType().name())
//	                .putData("source", request.getSource())
//	                .putData("destination", request.getDestination())
//	                .putData("title", "New Booking Available")
//	                .putData("body", request.getSource() + " → " + request.getDestination())

//	                .build();
			
			log.info("message "+message);

			try {
			    firebaseMessaging.send(message);
			} catch (FirebaseMessagingException e) {
			    log.error("Failed to send RIDE_REQUEST notification to vehicle {}", vehicle.getId(), e);
			}
	    }
	}



	public void notifyRideTaken(TransferRequestDetails request) {
	    List<Vehicle> vehicles = vehicleRepository.findAll();

	    for (Vehicle vehicle : vehicles) {
	        if (vehicle.getFcmToken() == null || vehicle.getFcmToken().isEmpty()) {
	            continue;
	        }

	        Message message = Message
	                .builder()
	                .setToken(vehicle.getFcmToken())
	                .putData("type", "RIDE_TAKEN")
	                .putData("requestId", request.getId().toString())
	                .setAndroidConfig(
	                        AndroidConfig.builder()
	                                .setPriority(AndroidConfig.Priority.HIGH)
	                                .build()
	                )
	                .build();

	        try {
	            firebaseMessaging.send(message);
	        } catch (FirebaseMessagingException e) {
	            log.error("Failed to send RIDE_TAKEN to vehicle {}", vehicle.getId(), e);
	        }
	    }
	}
	
}
