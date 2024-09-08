package com.samadhan.dto;
import lombok.Data;

import java.util.List;
import java.util.Map;

import com.samadhan.entity.Driver;
import com.samadhan.entity.ServiceCentre;

@Data
public class NotificationMessage {

	//private String recipientToken;
	private String title;
	private String body;
	private String image;
	private Map<String, String> data;
//	private String destinationLatitude;
//	private String destinationLongitude;
	//private List<ServiceCentre> serviceCentre;
	private List<Driver> drivers;
	
	

}
