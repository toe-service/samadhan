package com.samadhan.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;

// A vendor/individual owner declaring "I'll be near <toLocation> on <expectedDate>" ahead of
// time, so ride-matching can surface nearby requests to them before they actually arrive there —
// same idea as TransferVendor's fixed vendor_latitude/vendor_longitude, but a future, dated,
// second location instead of the vendor's permanent one.
@Entity
@Table(name = "vendor_availability")
public class VendorAvailability {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "vendor_id", nullable = false)
	@JsonBackReference
	private TransferVendor transferVendor;

	@Column(name = "from_location")
	private String fromLocation;

	@Column(name = "from_latitude")
	private String fromLatitude;

	@Column(name = "from_longitude")
	private String fromLongitude;

	@Column(name = "to_location")
	private String toLocation;

	@Column(name = "to_latitude")
	private String toLatitude;

	@Column(name = "to_longitude")
	private String toLongitude;

	// Encoded polyline for the from->to driving route, fetched once from RouteService at
	// posting time and cached here so matching doesn't re-call the Maps API per request lookup.
	@Column(name = "route_polyline", columnDefinition = "TEXT")
	private String routePolyline;

	@Column(name = "expected_date", nullable = false)
	private LocalDate expectedDate;

	@Column(name = "vehicle_type")
	private String vehicleType;

	@Column(name = "vehicle_category")
	private String vehicleCategory;

	@Column(name = "vehicle_number")
	private String vehicleNumber;

	@Column(name = "is_active")
	private boolean active = true;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public TransferVendor getTransferVendor() {
		return transferVendor;
	}

	public void setTransferVendor(TransferVendor transferVendor) {
		this.transferVendor = transferVendor;
	}

	public String getFromLocation() {
		return fromLocation;
	}

	public void setFromLocation(String fromLocation) {
		this.fromLocation = fromLocation;
	}

	public String getFromLatitude() {
		return fromLatitude;
	}

	public void setFromLatitude(String fromLatitude) {
		this.fromLatitude = fromLatitude;
	}

	public String getFromLongitude() {
		return fromLongitude;
	}

	public void setFromLongitude(String fromLongitude) {
		this.fromLongitude = fromLongitude;
	}

	public String getToLocation() {
		return toLocation;
	}

	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
	}

	public String getToLatitude() {
		return toLatitude;
	}

	public void setToLatitude(String toLatitude) {
		this.toLatitude = toLatitude;
	}

	public String getToLongitude() {
		return toLongitude;
	}

	public void setToLongitude(String toLongitude) {
		this.toLongitude = toLongitude;
	}

	public String getRoutePolyline() {
		return routePolyline;
	}

	public void setRoutePolyline(String routePolyline) {
		this.routePolyline = routePolyline;
	}

	public LocalDate getExpectedDate() {
		return expectedDate;
	}

	public void setExpectedDate(LocalDate expectedDate) {
		this.expectedDate = expectedDate;
	}

	public String getVehicleType() {
		return vehicleType;
	}

	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}

	public String getVehicleCategory() {
		return vehicleCategory;
	}

	public void setVehicleCategory(String vehicleCategory) {
		this.vehicleCategory = vehicleCategory;
	}

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
