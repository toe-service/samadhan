package com.samadhan.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


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
	
	@Column(name="user_email")
	private String userEmail;

	@Column(name="device_city")
	private String deviceCity;

	@Column(name="device_state")
	private String deviceState;

	@Column(name = "otp")
	private int otp;

	@Column(name = "device_id")
	private String deviceId;
}
