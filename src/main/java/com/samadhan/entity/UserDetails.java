package com.samadhan.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;


@Entity
@Table(name="user_details")
@Data
public class UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="user_name")
	private String userName;
	
	@Column(name="user_contact_number")
	private String userContactNumber;

	@JsonIgnore
	@Column(name = "user_password")
	private String userPassword;

	@Column(name="user_email")
	private String userEmail;

	@Column(name="device_city")
	private String deviceCity;

	@Column(name="device_state")
	private String deviceState;

	@JsonIgnore
	@Column(name = "otp")
	private int otp;

	@Column(name = "device_id")
	private String deviceId;

	@Column(name = "user_role")
	private String userRole;

	@Column(name = "is_active")
	private Boolean isActive = true;

	@Column(name = "last_login")
	private Long lastLogin;

	// Forgot-password OTP state — never serialized to the frontend. reset_otp_hash is a bcrypt
	// hash of the OTP (see LoginService), not the OTP itself.
	@JsonIgnore
	@Column(name = "reset_otp_hash")
	private String resetOtpHash;

	@JsonIgnore
	@Column(name = "reset_otp_expiry")
	private LocalDateTime resetOtpExpiry;

	@JsonIgnore
	@Column(name = "reset_otp_attempts")
	private Integer resetOtpAttempts;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserContactNumber() {
		return userContactNumber;
	}

	public void setUserContactNumber(String userContactNumber) {
		this.userContactNumber = userContactNumber;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getDeviceCity() {
		return deviceCity;
	}

	public void setDeviceCity(String deviceCity) {
		this.deviceCity = deviceCity;
	}

	public String getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(String deviceState) {
		this.deviceState = deviceState;
	}

	public int getOtp() {
		return otp;
	}

	public void setOtp(int otp) {
		this.otp = otp;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getUserRole() {
		return userRole;
	}

	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public Long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Long lastLogin) {
		this.lastLogin = lastLogin;
	}
}
