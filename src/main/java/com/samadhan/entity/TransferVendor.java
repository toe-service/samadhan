package com.samadhan.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.samadhan.enums.VendorStatusEnum;

//@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name="transfer_vendor")
public class TransferVendor {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="vendor_name")
	private String vendorName;
	
	@OneToMany(mappedBy = "transferVendor", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Driver> drivers;
	
	@OneToMany(mappedBy = "transferVendor", cascade = CascadeType.ALL)
	private List<Vehicle> vehicles;
	
	 @Column(name="vendor_latitude")
	 private String vendorLatitude;
	 
	 @Column(name="vendor_longitude")
	 private String vendorLongitude;
	 
	 @Column(name="vendor_email")
	 private String vendorEmail;
	 
	 @Column(name="vendor_password")
	 private String vendorPassword;
	 
	 @Column(name="vendor_city")
	 private String vendorCity;
	 
	 @Column(name="vendor_address")
	 private String vendorAddress;
	 
	 @Column(name="gst_number")
	 private String gstNumber;
	 
	 @OneToMany(mappedBy = "transferVendor")
	 private List<TransferRequestDetails> transferRequests;
	 
	 @Column(name="vendor_contact_number")
	 private String vendorContactNumber;
	 
	 @Column(name="vendor_status")
	 private VendorStatusEnum vendorStatus;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public List<Driver> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<Driver> drivers) {
		this.drivers = drivers;
	}

	public List<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public String getVendorLatitude() {
		return vendorLatitude;
	}

	public void setVendorLatitude(String vendorLatitude) {
		this.vendorLatitude = vendorLatitude;
	}

	public String getVendorLongitude() {
		return vendorLongitude;
	}

	public void setVendorLongitude(String vendorLongitude) {
		this.vendorLongitude = vendorLongitude;
	}

	public String getVendorEmail() {
		return vendorEmail;
	}

	public void setVendorEmail(String vendorEmail) {
		this.vendorEmail = vendorEmail;
	}

	public String getVendorPassword() {
		return vendorPassword;
	}

	public void setVendorPassword(String vendorPassword) {
		this.vendorPassword = vendorPassword;
	}

	public String getVendorCity() {
		return vendorCity;
	}

	public void setVendorCity(String vendorCity) {
		this.vendorCity = vendorCity;
	}

	public String getVendorAddress() {
		return vendorAddress;
	}

	public void setVendorAddress(String vendorAddress) {
		this.vendorAddress = vendorAddress;
	}

	public String getVendorContactNumber() {
		return vendorContactNumber;
	}

	public void setVendorContactNumber(String vendorContactNumber) {
		this.vendorContactNumber = vendorContactNumber;
	}

	public List<TransferRequestDetails> getTransferRequests() {
		return transferRequests;
	}

	public void setTransferRequests(List<TransferRequestDetails> transferRequests) {
		this.transferRequests = transferRequests;
	}

	public VendorStatusEnum getVendorStatus() {
		return vendorStatus;
	}

	public void setVendorStatus(VendorStatusEnum vendorStatus) {
		this.vendorStatus = vendorStatus;
	}

	public String getGstNumber() {
		return gstNumber;
	}

	public void setGstNumber(String gstNumber) {
		this.gstNumber = gstNumber;
	}
	
	

}
