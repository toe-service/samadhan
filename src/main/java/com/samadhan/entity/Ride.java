package com.samadhan.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name="rides")
public class Ride {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="ride_status")
	private boolean rideStatus;
	
	@Column(name="ride_otp")
	private int rideOtp;
	
	@Column(name="driver_response")
	private boolean driverResponse;
	
	@Column(name="driver_declination_reason")
	private String driverDeclinationReason;
	
	@ManyToOne
	@JoinColumn(name = "driver_id")
	private Driver driver;

	 @OneToOne(cascade = CascadeType.ALL)
	 @JoinColumn(name = "user_id", referencedColumnName = "id")
	 private User user;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean isRideStatus() {
		return rideStatus;
	}

	public void setRideStatus(boolean rideStatus) {
		this.rideStatus = rideStatus;
	}

	public int getRideOtp() {
		return rideOtp;
	}

	public void setRideOtp(int rideOtp) {
		this.rideOtp = rideOtp;
	}

	public boolean isDriverResponse() {
		return driverResponse;
	}

	public void setDriverResponse(boolean driverResponse) {
		this.driverResponse = driverResponse;
	}

	public String getDriverDeclinationReason() {
		return driverDeclinationReason;
	}

	public void setDriverDeclinationReason(String driverDeclinationReason) {
		this.driverDeclinationReason = driverDeclinationReason;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
}
