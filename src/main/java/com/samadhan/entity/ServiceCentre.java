package com.samadhan.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.samadhan.enums.serviceTypeEnum;

@Entity
@Table(name="Service_centre")
public class ServiceCentre {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="service_centre_name")
	private String serviceCentreName;
	
	@Column(name="contact_number")
	private Long contactNumber;
	
	@Column(name="email")
	private Long email;
	
	@Column(name="active")
	private boolean active;
	
	@Column(name="location")
	private Location location;
	
	@Column(name="payment")
	private Payment payment;
	
	@Column(name="service_type")
	private serviceTypeEnum serviceType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getServiceCentreName() {
		return serviceCentreName;
	}

	public void setServiceCentreName(String serviceCentreName) {
		this.serviceCentreName = serviceCentreName;
	}

	public Long getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(Long contactNumber) {
		this.contactNumber = contactNumber;
	}

	public Long getEmail() {
		return email;
	}

	public void setEmail(Long email) {
		this.email = email;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public serviceTypeEnum getServiceType() {
		return serviceType;
	}

	public void setServiceType(serviceTypeEnum serviceType) {
		this.serviceType = serviceType;
	}
	
	
}
