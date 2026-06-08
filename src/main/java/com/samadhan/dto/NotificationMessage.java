package com.samadhan.dto;
import java.util.List;
import java.util.Map;

import com.samadhan.entity.Driver;
import com.samadhan.entity.ServiceCentre;

public class NotificationMessage {

	//private String recipientToken;
	private String title;
	private String body;
	private String image;
	private Map<String, String> data;
//	private String destinationLatitude;
//	private String destinationLongitude;
	//private List<ServiceCentre> serviceCentre;
	private List<ServiceCentreWrapper> drivers;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public Map<String, String> getData() {
		return data;
	}

	public void setData(Map<String, String> data) {
		this.data = data;
	}

	public List<ServiceCentreWrapper> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<ServiceCentreWrapper> drivers) {
		this.drivers = drivers;
	}
}
