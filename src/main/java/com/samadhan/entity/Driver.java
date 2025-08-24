package com.samadhan.entity;

import lombok.Data;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="driver")
public class Driver {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="driver_name")
	private String driverName;
	
	@Column(name="driver_contact_number")
	private String driverContactNumber;
	
	@Column(name="driver_email")
	private String driverEmail;
	
	@Column(name="driver_active")
	private boolean driverActive;
	
	@Column(name="driver_city")
	private String driverCity;
	
	@Column(name="driver_token")
	private String driverToken;
	
	@OneToMany(mappedBy = "driver", cascade = CascadeType.ALL)
	private List<Ride> rides;
	
	@Column(name="driver_latitude")
	private String driverLatitude;
	
	@Column(name="driver_longitude")
	private String driverLongitude;
	
	@ManyToOne
//	@JoinColumn(name = "service_centre_id")
	@JsonIgnore
	private ServiceCentre serviceCentre;

	@Transient
	private String destinationLatitude;

	@Transient
	private String destinationLongitude;

	public String getDestinationLatitude() {
		return destinationLatitude;
	}

	public String getDriverLongitude() {
		return driverLongitude;
	}

	public String getDriverLatitude() {
		return driverLatitude;
	}

	public String getDestinationLongitude() {
		return destinationLongitude;
	}

	public void setDestinationLatitude(String destinationLatitude) {
		this.destinationLatitude = destinationLatitude;
	}

	public void setDestinationLongitude(String destinationLongitude) {
		this.destinationLongitude = destinationLongitude;
	}

	public String isDriverCity() {
		return driverCity;
	}

	public void setDriverCity(String driverCity) {
		this.driverCity = driverCity;
	}

	public List<Ride> getRides() {
		return rides;
	}

	public void setRides(List<Ride> rides) {
		this.rides = rides;
	}

	public String isDriverLatitude() {
		return driverLatitude;
	}

	public void setDriverLatitude(String driverLatitude) {
		this.driverLatitude = driverLatitude;
	}

	public String isDriverLongitude() {
		return driverLongitude;
	}

	public void setDriverLongitude(String driverLongitude) {
		this.driverLongitude = driverLongitude;
	}

	public ServiceCentre getServiceCentre() {
		return serviceCentre;
	}

	public void setServiceCentre(ServiceCentre serviceCentre) {
		this.serviceCentre = serviceCentre;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverContactNumber() {
		return driverContactNumber;
	}

	public void setDriverContactNumber(String driverContactNumber) {
		this.driverContactNumber = driverContactNumber;
	}

	public String getDriverEmail() {
		return driverEmail;
	}

	public void setDriverEmail(String driverEmail) {
		this.driverEmail = driverEmail;
	}

	public boolean isDriverActive() {
		return driverActive;
	}

	public void setDriverActive(boolean driverActive) {
		this.driverActive = driverActive;
	}

	public String getDriverToken() {
		return driverToken;
	}

	public void setDriverToken(String driverToken) {
		this.driverToken = driverToken;
	}

	public List<Ride> getDriverRides() {
		return rides;
	}

	public void setDriverRides(List<Ride> driverRides) {
		this.rides = rides;
	}

	
}
