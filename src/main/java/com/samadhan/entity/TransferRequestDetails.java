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
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.samadhan.enums.DimensionUnit;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;

@Entity
@Table(name="transfer_request_details")
public class TransferRequestDetails {
	

	public DimensionUnit getDimensionUnit() {
		return dimensionUnit;
	}

	public void setDimensionUnit(DimensionUnit dimensionUnit) {
		this.dimensionUnit = dimensionUnit;
	}

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
	 
	 @Column(name="user_type")
	 private String userType;
	 
	 @Column(name="destination")
	 private String destination;
	 
	 @Column(name="source_latitude")
	 private String sourceLatitude;
	 
	 @Column(name="source_longitude")
	 private String sourceLongitude;
	 
	 @Column(name="source")
	 private String source;
	 
	 @Column(name="dimension_unit")
	 private DimensionUnit dimensionUnit;

	 @OneToOne(cascade = CascadeType.MERGE)
	 @JoinColumn(name = "user_id", referencedColumnName = "id")
	// @JsonIgnore
	 private UserDetails userDetails;
	 
	 @OneToOne(cascade = CascadeType.ALL)
	 @JoinColumn(name = "parcel_details_id", referencedColumnName = "id")
	 private ParcelDetails parcelDetails;
	 
	 @OneToOne(cascade = CascadeType.MERGE)
	 @JoinColumn(name = "driver_id", referencedColumnName = "id")
	// @JsonIgnore
	 private Driver driver;
	 
	 @ManyToOne(cascade = CascadeType.MERGE)
	 @JoinColumn(name = "transfer_id", referencedColumnName = "id")
	 @JsonIgnoreProperties({ "transferRequests","drivers", "vehicles",
		    "vendorPassword"})
	 private TransferVendor transferVendor;
	 
	 @Column(name="vehicle_type")
	 private VehicleTypeEnum VehicleType;
	 
	 @Column(name="parcel_type")
	 private ParcelTypeEnum ParcelType;
	 
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
	 private Integer otp;
	 
	 @Column(name="closure_otp")
	 private Integer  closureotp;
	 
	 @OneToOne(cascade = CascadeType.MERGE)
	 @JoinColumn(name = "vehicle_id", referencedColumnName = "id")
	 private Vehicle vehicleId;
	 
//	 @Column(name="vehicle_id")
//	 private Integer vehicleId;
	 
	 @Column(name="request_created_date")
	 private LocalDateTime requestCreatedDate;
	 
	 @Column(name="request_approval_date")
	 private LocalDateTime RequestApprovalDate;
	 
	 @Column(name="driver_assigned_date")
	 private LocalDateTime DriverAssignDateTime;
	 
	 @Column(name="handovered_date")
	 private LocalDateTime HandoveredDateTime;
	 
	 @Column(name="vehicle_assign_date")
	 private LocalDateTime VehicleAssignDateTime;
	 
	 @Column(name = "distance_km", insertable = false, updatable = false)
	 private Double distanceKm;
	 
	 @Column(name = "ride_without_tax")
	 private Double rideWithoutTaxCalculation;
	 
	 @Column(name = "gst_cost")
	 private Double gstCost;
	 
	 @Column(name = "loading_Unloading_cost")
	 private Double loadingUnloading;
	 
	 @Column(name = "packaging_cost")
	 private Double packagingCost;
	 
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

	public Integer getOtp() {
		return otp;
	}

	public void setOtp(Integer otp) {
		this.otp = otp;
	}

//	public Integer getVehicleId() {
//		return vehicleId;
//	}
//
//	public void setVehicleId(Integer vehicleId) {
//		this.vehicleId = vehicleId;
//	}

	public LocalDateTime getRequestApprovalDate() {
		return RequestApprovalDate;
	}

	public Vehicle getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(Vehicle vehicleId) {
		this.vehicleId = vehicleId;
	}

	public void setRequestApprovalDate(LocalDateTime requestApprovalDate) {
		RequestApprovalDate = requestApprovalDate;
	}

	public LocalDateTime getDriverAssignDateTime() {
		return DriverAssignDateTime;
	}

	public void setDriverAssignDateTime(LocalDateTime driverAssignDateTime) {
		DriverAssignDateTime = driverAssignDateTime;
	}

	public LocalDateTime getHandoveredDateTime() {
		return HandoveredDateTime;
	}

	public void setHandoveredDateTime(LocalDateTime handoveredDateTime) {
		HandoveredDateTime = handoveredDateTime;
	}

	public TransferVendor getTransferVendor() {
		return transferVendor;
	}

	public void setTransferVendor(TransferVendor transferVendor) {
		this.transferVendor = transferVendor;
	}

	public Integer getClosureotp() {
		return closureotp;
	}

	public void setClosureotp(Integer closureotp) {
		this.closureotp = closureotp;
	}

	public LocalDateTime getVehicleAssignDateTime() {
		return VehicleAssignDateTime;
	}

	public void setVehicleAssignDateTime(LocalDateTime vehicleAssignDateTime) {
		VehicleAssignDateTime = vehicleAssignDateTime;
	}

//	public TransferVendor gettransferVendor() {
//		return transferVendor;
//	}
//
//	public void settransferVendor(TransferVendor transferVendor) {
//		this.transferVendor = transferVendor;
//	}


	public Double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(Double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public LocalDateTime getRequestCreatedDate() {
		return requestCreatedDate;
	}

	public void setRequestCreatedDate(LocalDateTime requestCreatedDate) {
		this.requestCreatedDate = requestCreatedDate;
	}

	public ParcelTypeEnum getParcelType() {
		return ParcelType;
	}

	public void setParcelType(ParcelTypeEnum parcelType) {
		ParcelType = parcelType;
	}

	public ParcelDetails getParcelDetails() {
		return parcelDetails;
	}

	public void setParcelDetails(ParcelDetails parcelDetails) {
		this.parcelDetails = parcelDetails;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public Double getRideWithoutTaxCalculation() {
		return rideWithoutTaxCalculation;
	}

	public void setRideWithoutTaxCalculation(Double rideWithoutTaxCalculation) {
		this.rideWithoutTaxCalculation = rideWithoutTaxCalculation;
	}

	public Double getGstCost() {
		return gstCost;
	}

	public void setGstCost(Double gstCost) {
		this.gstCost = gstCost;
	}

	public Double getLoadingUnloading() {
		return loadingUnloading;
	}

	public void setLoadingUnloading(Double loadingUnloading) {
		this.loadingUnloading = loadingUnloading;
	}

	public Double getPackagingCost() {
		return packagingCost;
	}

	public void setPackagingCost(Double packagingCost) {
		this.packagingCost = packagingCost;
	}

	 
}
