package com.samadhan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;

@Entity
@Table(name="transfer_request_details")
public class TransferRequestDetails {
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
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
	 
	 @Column(name="destination")
	 private String destination;
	 
	 @Column(name="source_latitude")
	 private String sourceLatitude;
	 
	 @Column(name="source_longitude")
	 private String sourceLongitude;
	 
	 @Column(name="source")
	 private String source;

	 @OneToOne(cascade = CascadeType.ALL)
	 @JoinColumn(name = "user_id", referencedColumnName = "id")
	 @JsonIgnore
	 private UserDetails userDetails;
	 
	 @OneToOne(cascade = CascadeType.ALL)
	 @JoinColumn(name = "driver_id", referencedColumnName = "id")
	// @JsonIgnore
	 private Driver driver;
	 
	 @Column(name="vehicle_type")
	 private VehicleTypeEnum VehicleType;
	 
	 @Column(name="ride_cost")
	 private double rideCost;

	 @Column(name="pickup_date")
	 private LocalDate pickupDate;
	 
	 @Column(name="pickup_time")
	 private LocalTime pickupTime;
	 
	 @Column(name="pickup_schedule")
	 private String pickupSchedule;
	 
	 @Column(name="transfer_status")
	 private rideStatusEnum transferStatus;
	 
	 @Column(name="otp")
	 private int otp;

	public Long getId() {
		return id;
	}

	public rideStatusEnum getTransferStatus() {
		return transferStatus;
	}

	public void setTransferStatus(rideStatusEnum transferStatus) {
		this.transferStatus = transferStatus;
	}

	public void setId(Long id) {
		this.id = id;
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

	public UserDetails getUserDetails() {
		return userDetails;
	}

	public void setUserDetails(UserDetails userDetails) {
		this.userDetails = userDetails;
	}

	public VehicleTypeEnum getVehicleType() {
		return VehicleType;
	}

	public void setVehicleType(VehicleTypeEnum vehicleType) {
		VehicleType = vehicleType;
	}

	public double getRideCost() {
		return rideCost;
	}

	public void setRideCost(double rideCost) {
		this.rideCost = rideCost;
	}

	public LocalDate getPickupDate() {
		return pickupDate;
	}

	public void setPickupDate(LocalDate pickupDate) {
		this.pickupDate = pickupDate;
	}

	public LocalTime getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(LocalTime pickupTime) {
		this.pickupTime = pickupTime;
	}

	public String getPickupSchedule() {
		return pickupSchedule;
	}

	public void setPickupSchedule(String pickupSchedule) {
		this.pickupSchedule = pickupSchedule;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getOtp() {
		return otp;
	}

	public void setOtp(int otp) {
		this.otp = otp;
	}

//	public double getTransferCalculation() {
//		return transferCalculation;
//	}
//
//	public void setTransferCalculation(double transferCalculation) {
//		this.transferCalculation = transferCalculation;
//	}
	 
	 
	 
}
