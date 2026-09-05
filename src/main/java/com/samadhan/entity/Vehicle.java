package com.samadhan.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;

@Entity
@Table(name="vehicle")
public class Vehicle {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="vehicle_number")
	private String vehicleNumber;
	
	@Column(name="vehicle_contact_number")
	private String vehicleContactNumber;
	
	@ManyToOne
	@JoinColumn(name = "transfer_id")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private TransferVendor transferVendor;
	
	@Column(name="user_name")
	private String userName;
	
	@JsonIgnore
	@Column(name="password")
	private String password;

	// Forgot-password OTP state — never serialized to the frontend. reset_otp_hash is a bcrypt
	// hash of the OTP (see LoginService), not the OTP itself.
	@JsonIgnore
	@Column(name = "reset_otp_hash")
	private String resetOtpHash;

	@JsonIgnore
	@Column(name = "reset_otp_expiry")
	private java.time.LocalDateTime resetOtpExpiry;

	@JsonIgnore
	@Column(name = "reset_otp_attempts")
	private Integer resetOtpAttempts;

	public String getResetOtpHash() {
		return resetOtpHash;
	}

	public void setResetOtpHash(String resetOtpHash) {
		this.resetOtpHash = resetOtpHash;
	}

	public java.time.LocalDateTime getResetOtpExpiry() {
		return resetOtpExpiry;
	}

	public void setResetOtpExpiry(java.time.LocalDateTime resetOtpExpiry) {
		this.resetOtpExpiry = resetOtpExpiry;
	}

	public Integer getResetOtpAttempts() {
		return resetOtpAttempts;
	}

	public void setResetOtpAttempts(Integer resetOtpAttempts) {
		this.resetOtpAttempts = resetOtpAttempts;
	}

	@Column(name="current_location")
	private String currentLocation;
	
	@Column(name="Ongoing_status", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private boolean ongoingStatus;
	
	@Column(name="vendor_vehicle_type", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private VendorPickupVehicleEnum vendorVehicle;
	
	@Column(name="vehicle_category", columnDefinition = "BOOLEAN DEFAULT FALSE")
	private VehicleCategoryEnum VehicleCategory;
	
	@Column(name="vehicle_latitude")
	private String vehicleLatitude;
	
	@Column(name="fcm_token")
	private String fcmToken;
	
	@Column(name="vehicle_longitude")
	private String vehicleLongitude;

	@Column(name="rc_storage_key")
	private String rcStorageKey;


	public VehicleCategoryEnum getVehicleCategory() {
		return VehicleCategory;
	}

	public void setVehicleCategory(VehicleCategoryEnum vehicleCategory) {
		VehicleCategory = vehicleCategory;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public TransferVendor getTransferVendor() {
		return transferVendor;
	}

	public void setTransferVendor(TransferVendor transferVendor) {
		this.transferVendor = transferVendor;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}

	public boolean getOngoingStatus() {
		return ongoingStatus;
	}

	public void setOngoingStatus(boolean ongoingStatus) {
		this.ongoingStatus = ongoingStatus;
	}

	public String getVehicleContactNumber() {
		return vehicleContactNumber;
	}

	public void setVehicleContactNumber(String vehicleContactNumber) {
		this.vehicleContactNumber = vehicleContactNumber;
	}

	public VendorPickupVehicleEnum getVendorVehicle() {
		return vendorVehicle;
	}

	public void setVendorVehicle(VendorPickupVehicleEnum vendorVehicle) {
		this.vendorVehicle = vendorVehicle;
	}

	// Human-readable vehicle type ("Tata Ace", "14ft Closed", ...) alongside the raw
	// vendorVehicle enum constant, for clients that just want a display label.
	public String getVehicleType() {
		return vendorVehicle != null ? vendorVehicle.getDisplayName() : null;
	}

	public String getVehicleLatitude() {
		return vehicleLatitude;
	}

	public void setVehicleLatitude(String vehicleLatitude) {
		this.vehicleLatitude = vehicleLatitude;
	}

	public String getFcmToken() {
		return fcmToken;
	}

	public void setFcmToken(String fcmToken) {
		this.fcmToken = fcmToken;
	}

	public String getVehicleLongitude() {
		return vehicleLongitude;
	}

	public void setVehicleLongitude(String vehicleLongitude) {
		this.vehicleLongitude = vehicleLongitude;
	}

	public String getRcStorageKey() {
		return rcStorageKey;
	}

	public void setRcStorageKey(String rcStorageKey) {
		this.rcStorageKey = rcStorageKey;
	}

}
