package com.samadhan.entity;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
	 
	 @JsonIgnore
	 @Column(name="vendor_password")
	 private String vendorPassword;
	 
	 @Column(name="vendor_city")
	 private String vendorCity;
	 
	 @Column(name="vendor_address")
	 private String vendorAddress;
	 
	 @Column(name="gst_number")
	 private String gstNumber;
	 
	 @OneToMany(mappedBy = "transferVendor")
	 @JsonIgnore
	 private List<TransferRequestDetails> transferRequests;
	 
	 @Column(name="vendor_contact_number")
	 private String vendorContactNumber;
	 
	 @Column(name="vendor_status")
	 private VendorStatusEnum vendorStatus;
	 
	 @Column(name = "aadhaar_storage_key")
	 private String aadhaarStorageKey;

	 @Column(name = "pan_storage_key")
	 private String panStorageKey;

	 @Column(name = "is_individual")
	 private Boolean isIndividual;

	 // Legal record of Terms & Conditions acceptance at registration time. termsText is a full
	 // snapshot of the exact wording shown to the vendor (not just a version number) so that if
	 // the Terms are edited later, there is still an immutable record of what was actually
	 // agreed to — a version string alone would rely on the app's source history not changing
	 // retroactively, which isn't a safe assumption for a legal record.
	 @Column(name = "terms_accepted")
	 private Boolean termsAccepted;

	 @Column(name = "terms_accepted_at")
	 private LocalDateTime termsAcceptedAt;

	 @Column(name = "terms_version")
	 private String termsVersion;

	 @Lob
	 @Column(name = "terms_text", columnDefinition = "LONGTEXT")
	 private String termsText;

	 @OneToOne(mappedBy = "vendor", fetch = FetchType.LAZY)
	 @JsonManagedReference
	 private Subscription subscription;
	 
	 @OneToMany(mappedBy = "transferVendor", cascade = CascadeType.ALL, orphanRemoval = true)
	 @JsonManagedReference
	 private List<VendorService> vendorServices;

	 public List<VendorService> getVendorServices() {
	     return vendorServices;
	 }

	 public void setVendorServices(List<VendorService> vendorServices) {
	     this.vendorServices = vendorServices;
	 }
	
	public String getAadhaarStorageKey() {
		return aadhaarStorageKey;
	}

	public void setAadhaarStorageKey(String aadhaarStorageKey) {
		this.aadhaarStorageKey = aadhaarStorageKey;
	}

	public String getPanStorageKey() {
		return panStorageKey;
	}

	public void setPanStorageKey(String panStorageKey) {
		this.panStorageKey = panStorageKey;
	}

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

	public Boolean getIsIndividual() {
		return isIndividual;
	}

	public void setIsIndividual(Boolean isIndividual) {
		this.isIndividual = isIndividual;
	}

	public Boolean getTermsAccepted() {
		return termsAccepted;
	}

	public void setTermsAccepted(Boolean termsAccepted) {
		this.termsAccepted = termsAccepted;
	}

	public LocalDateTime getTermsAcceptedAt() {
		return termsAcceptedAt;
	}

	public void setTermsAcceptedAt(LocalDateTime termsAcceptedAt) {
		this.termsAcceptedAt = termsAcceptedAt;
	}

	public String getTermsVersion() {
		return termsVersion;
	}

	public void setTermsVersion(String termsVersion) {
		this.termsVersion = termsVersion;
	}

	public String getTermsText() {
		return termsText;
	}

	public void setTermsText(String termsText) {
		this.termsText = termsText;
	}

}
