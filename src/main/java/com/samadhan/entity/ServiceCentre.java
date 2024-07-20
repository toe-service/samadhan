package com.samadhan.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	private String email;
	
	@Column(name="active")
	private boolean active;
	
	@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", referencedColumnName = "id")
	@JsonIgnore
    private Location location;
	
	@OneToMany(mappedBy = "serviceCentre", cascade = CascadeType.ALL)
	private List<Payment> payment;
	
	@OneToMany(mappedBy = "serviceCentre", cascade = CascadeType.ALL)
	private List<Driver> drivers;
	
//	@OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_id", referencedColumnName = "id")
//	//@Column(name="payment")
//	private Payment payment;
	
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

//	public Payment getPayment() {
//		return payment;
//	}
//
//	public void setPayment(Payment payment) {
//		this.payment = payment;
//	}
//
//	public serviceTypeEnum getServiceType() {
//		return serviceType;
//	}
	
	

	public void setServiceType(serviceTypeEnum serviceType) {
		this.serviceType = serviceType;
	}

	public List<Payment> getPayment() {
		return payment;
	}

	public void setPayment(List<Payment> payment) {
		this.payment = payment;
	}

	public serviceTypeEnum getServiceType() {
		return serviceType;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<Driver> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<Driver> drivers) {
		this.drivers = drivers;
	}
	
	
}
