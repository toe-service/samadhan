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

	 // Per-vendor GST invoice numbering (PaymentController#generateInvoice) — a clean sequential
	 // series per supplier per financial year, reset to 0 whenever invoiceFinancialYear no longer
	 // matches the current FY. Not shared across vendors: each vendor is its own GST "supplier",
	 // so their invoice series must be independent of everyone else's on the platform.
	 @Column(name="invoice_sequence")
	 private Integer invoiceSequence = 0;

	 @Column(name="invoice_financial_year")
	 private String invoiceFinancialYear;

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

	 @Column(name = "signature_storage_key")
	 private String signatureStorageKey;

	 @Column(name = "is_individual")
	 private Boolean isIndividual;

	 // Separate from vendorStatus/isSubscriber() — this gates whether an INDIVIDUAL (owner-
	 // operator) vendor has paid the one-time Rs 199 registration fee (see
	 // PaymentController's /pay/registration-fee endpoints), not whether they've bought a
	 // subscription plan. Only ever checked for isIndividual=true vendors (see
	 // VehicleServiceImpl#createVehicle); a non-individual vendor's value here is simply never
	 // consulted, so it staying false by default for everyone is harmless.
	 @Column(name = "registration_fee_paid")
	 private boolean registrationFeePaid;

	 public boolean isRegistrationFeePaid() {
		 return registrationFeePaid;
	 }

	 public void setRegistrationFeePaid(boolean registrationFeePaid) {
		 this.registrationFeePaid = registrationFeePaid;
	 }

	 // Vendor-editable public-page branding content (see TransferVendorController's
	 // /profile-content and /profile-image endpoints) -- all optional, all null until a vendor
	 // deliberately sets them from the dashboard. getPublicVendorProfile falls back to generated
	 // copy (vendor name, city, services) when these are blank, so an un-customized page still
	 // looks complete rather than empty.
	 @Column(name = "business_tagline")
	 private String businessTagline;

	 @Lob
	 @Column(name = "about_text", columnDefinition = "TEXT")
	 private String aboutText;

	 @Column(name = "logo_storage_key")
	 private String logoStorageKey;

	 @Column(name = "cover_image_storage_key")
	 private String coverImageStorageKey;

	 public String getBusinessTagline() {
		 return businessTagline;
	 }

	 public void setBusinessTagline(String businessTagline) {
		 this.businessTagline = businessTagline;
	 }

	 public String getAboutText() {
		 return aboutText;
	 }

	 public void setAboutText(String aboutText) {
		 this.aboutText = aboutText;
	 }

	 public String getLogoStorageKey() {
		 return logoStorageKey;
	 }

	 public void setLogoStorageKey(String logoStorageKey) {
		 this.logoStorageKey = logoStorageKey;
	 }

	 public String getCoverImageStorageKey() {
		 return coverImageStorageKey;
	 }

	 public void setCoverImageStorageKey(String coverImageStorageKey) {
		 this.coverImageStorageKey = coverImageStorageKey;
	 }

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

	 // Set to a fresh random value on every login, embedded in that login's JWT as its "jti" —
	 // JwtAuthenticationFilter compares the two on every authenticated request, so logging in
	 // again anywhere immediately invalidates every token from the previous login, instead of
	 // waiting for it to expire. See TokenApi#generateToken(..., sessionId) and
	 // JwtAuthenticationFilter#isSessionInvalid.
	 @JsonIgnore
	 @Column(name = "current_session_id")
	 private String currentSessionId;

	 public String getResetOtpHash() {
		 return resetOtpHash;
	 }

	 public void setResetOtpHash(String resetOtpHash) {
		 this.resetOtpHash = resetOtpHash;
	 }

	 public LocalDateTime getResetOtpExpiry() {
		 return resetOtpExpiry;
	 }

	 public void setResetOtpExpiry(LocalDateTime resetOtpExpiry) {
		 this.resetOtpExpiry = resetOtpExpiry;
	 }

	 public Integer getResetOtpAttempts() {
		 return resetOtpAttempts;
	 }

	 public void setResetOtpAttempts(Integer resetOtpAttempts) {
		 this.resetOtpAttempts = resetOtpAttempts;
	 }

	 public String getCurrentSessionId() {
		 return currentSessionId;
	 }

	 public void setCurrentSessionId(String currentSessionId) {
		 this.currentSessionId = currentSessionId;
	 }

	 // Running average of every customer rating (TransferRequestDetails#rating) left on a ride this
	 // vendor fulfilled (whether accepted directly or via an assigned vehicle) — same incremental-
	 // update reasoning as Vehicle#avgRating, updated in TransferRequestServiceImpl#rateRide.
	 @Column(name = "avg_rating")
	 private Double avgRating;

	 @Column(name = "rating_count")
	 private Integer ratingCount;

	 public Double getAvgRating() {
		 return avgRating;
	 }

	 public void setAvgRating(Double avgRating) {
		 this.avgRating = avgRating;
	 }

	 public Integer getRatingCount() {
		 return ratingCount;
	 }

	 public void setRatingCount(Integer ratingCount) {
		 this.ratingCount = ratingCount;
	 }

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

	public String getSignatureStorageKey() {
		return signatureStorageKey;
	}

	public void setSignatureStorageKey(String signatureStorageKey) {
		this.signatureStorageKey = signatureStorageKey;
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

	// In good standing on either the free trial or a paid plan — Free_SUBSCRIPTION covers the
	// 15-day trial, ACTIVE covers a paid plan. SUBSCRIPTION_PENDING (trial expired, not yet paid)
	// and SUSPENDED (paid plan lapsed) both fall through to false. Shared by every
	// subscription-gated business rule (wallet fee rate, fleet size cap, ...) so they all agree
	// on what "subscriber" means without duplicating this check in each service.
	public boolean isSubscriber() {
		return vendorStatus == VendorStatusEnum.Free_SUBSCRIPTION || vendorStatus == VendorStatusEnum.ACTIVE;
	}

	// One-time registration fee amount for THIS vendor — Rs 199 for an individual (owner-operator)
	// account, Rs 399 for a full (fleet) vendor. Centralized here rather than duplicated as a
	// ternary in PaymentController and every registrationFeePaid gate (VehicleServiceImpl,
	// VendorAvailabilityServiceImpl, TransferRequestServiceImpl) so the two numbers only ever live
	// in one place.
	public int getRegistrationFeeRupees() {
		return Boolean.TRUE.equals(isIndividual) ? 199 : 399;
	}

	public String getGstNumber() {
		return gstNumber;
	}

	public void setGstNumber(String gstNumber) {
		this.gstNumber = gstNumber;
	}

	public Integer getInvoiceSequence() {
		return invoiceSequence;
	}

	public void setInvoiceSequence(Integer invoiceSequence) {
		this.invoiceSequence = invoiceSequence;
	}

	public String getInvoiceFinancialYear() {
		return invoiceFinancialYear;
	}

	public void setInvoiceFinancialYear(String invoiceFinancialYear) {
		this.invoiceFinancialYear = invoiceFinancialYear;
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
