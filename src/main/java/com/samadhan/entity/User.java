package com.samadhan.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * TODO: NEED TO ADD LATITUDE AND LONGITUDE DURING USER LOGIN (done)
 */
@Entity
@Table(name="user")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="user_name")
	private String userName;
	
	@Column(name="user_contact_number")
	private Long userContactNumber;
	
	@Column(name="user_email")
	private String userEmail;

	@Column(name="user_latitude")
	private String userLatitude;

	@Column(name="user_longitude")
	private String userLongitude;

	public String getUserLatitude() {
		return userLatitude;
	}

	public void setUserLatitude(String userLatitude) {
		this.userLatitude = userLatitude;
	}

	public String getUserLongitude() {
		return userLongitude;
	}

	public void setUserLongitude(String userLongitude) {
		this.userLongitude = userLongitude;
	}

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

	public Long getUserContactNumber() {
		return userContactNumber;
	}

	public void setUserContactNumber(Long userContactNumber) {
		this.userContactNumber = userContactNumber;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	
}
