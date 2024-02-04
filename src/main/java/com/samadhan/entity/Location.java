package com.samadhan.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="location")
public class Location {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="city")
	private String city;
	
	@Column(name="state")
	private String state;
	
	@Column(name="area")
	private String area;
	
	@Column(name="street_name")
	private String streetName;
	
	@Column(name="shop_number")
	private Long shopNumber;
	
	@Column(name="latitude")
	private Long latitude;
	
	@Column(name="longitude")
	private Long longitude;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public Long getShopNumber() {
		return shopNumber;
	}

	public void setShopNumber(Long shopNumber) {
		this.shopNumber = shopNumber;
	}

	public Long getLatitude() {
		return latitude;
	}

	public void setLatitude(Long latitude) {
		this.latitude = latitude;
	}

	public Long getLongitude() {
		return longitude;
	}

	public void setLongitude(Long longitude) {
		this.longitude = longitude;
	}
	
	
	
	
}
