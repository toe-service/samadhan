package com.samadhan.dto;

public class RideCostSummary {
	
	double totalCost;
	double rideCost;
	double gst;
	double packaging;
	double LoadingUnloading;
	double pickupCost;
	String vendorVehicleType;
	double distanceInKm;
	double nonRunningCharge;
	public double getDistanceInKm() {
		return distanceInKm;
	}
	public void setDistanceInKm(double distanceInKm) {
		this.distanceInKm = distanceInKm;
	}
	public String getVendorVehicleType() {
		return vendorVehicleType;
	}
	public void setVendorVehicleType(String vendorVehicleType) {
		this.vendorVehicleType = vendorVehicleType;
	}
	public double getTotalCost() {
		return totalCost;
	}
	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}
	public double getGst() {
		return gst;
	}
	public void setGst(double gst) {
		this.gst = gst;
	}
	public double getPackaging() {
		return packaging;
	}
	public void setPackaging(double packaging) {
		this.packaging = packaging;
	}
	public double getLoadingUnloading() {
		return LoadingUnloading;
	}
	public void setLoadingUnloading(double loadingUnloading) {
		LoadingUnloading = loadingUnloading;
	}
	public double getPickupCost() {
		return pickupCost;
	}
	public void setPickupCost(double pickupCost) {
		this.pickupCost = pickupCost;
	}
	public double getRideCost() {
		return rideCost;
	}
	public void setRideCost(double rideCost) {
		this.rideCost = rideCost;
	}
	public double getNonRunningCharge() {
		return nonRunningCharge;
	}
	public void setNonRunningCharge(double nonRunningCharge) {
		this.nonRunningCharge = nonRunningCharge;
	}
	
	
	

}
