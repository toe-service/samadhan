package com.samadhan.entity;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="rides")
public class Ride {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "ride_id", unique = true)
	private String rideId;
	
	@Column(name="ride_status")
	private int rideStatus;
	
	@Column(name="ride_otp")
	private int rideOtp;
	
	@Column(name="driver_response")
	private boolean driverResponse;
	
	@Column(name="driver_declination_reason")
	private String driverDeclinationReason;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id")
	@JsonIgnore
	private Driver driver;

	 @OneToOne(cascade = CascadeType.ALL)
	 @JoinColumn(name = "user_id", referencedColumnName = "id")
	 @JsonIgnore
	 private User user;
	 
	 @Column(name="ride_response_time")
	 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	 private LocalDateTime rideResponseTime;
	 
	 @Column(name="ride_start_time")
	 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	 private LocalDateTime ridestartTime;
	 
	 @Column(name="ride_end_time")
	 @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	 private LocalDateTime rideendTime;
	 
	 @Column(name="destination_latitude")
	 private String destinationLatitude;
	
	 @Column(name="destination_longitude")
	 private String destinationLongitude;
	 
	 @Column(name="source_latitude")
	 private String sourceLatitude;
	 
	 @Column(name="source_longitude")
	 private String sourceLongitude;
	 
	 @Transient
	 private String driverName;
	 
	 @Transient
	 private String carNumber;
	 
	 
	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getCarNumber() {
		return carNumber;
	}

	public void setCarNumber(String carNumber) {
		this.carNumber = carNumber;
	}

	public String getDestinationLatitude() {
		return destinationLatitude;
	}

	public void setDestinationLatitude(String destinationLatitude) {
		this.destinationLatitude = destinationLatitude;
	}

	public String getDestinationLongitude() {
		return destinationLongitude;
	}

	public void setDestinationLongitude(String destinationLongitude) {
		this.destinationLongitude = destinationLongitude;
	}

	public String getSourceLatitude() {
		return sourceLatitude;
	}

	public void setSourceLatitude(String sourceLatitude) {
		this.sourceLatitude = sourceLatitude;
	}

	public String getSourceLongitude() {
		return sourceLongitude;
	}

	public void setSourceLongitude(String sourceLongitude) {
		this.sourceLongitude = sourceLongitude;
	}

	public LocalDateTime getRideResponseTime() {
		return rideResponseTime;
	}

	public void setRideResponseTime(LocalDateTime rideResponseTime) {
		this.rideResponseTime = rideResponseTime;
	}

	public LocalDateTime getRidestartTime() {
		return ridestartTime;
	}

	public void setRidestartTime(LocalDateTime ridestartTime) {
		this.ridestartTime = ridestartTime;
	}

	public LocalDateTime getRideendTime() {
		return rideendTime;
	}

	public void setRideendTime(LocalDateTime rideendTime) {
		this.rideendTime = rideendTime;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int isRideStatus() {
		return rideStatus;
	}

	public void setRideStatus(int rideStatus) {
		this.rideStatus = rideStatus;
	}

	public int getRideOtp() {
		return rideOtp;
	}

	public void setRideId(String rideId) {
		this.rideId = rideId;
	}

	public String getRideId() {
		return rideId;
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
