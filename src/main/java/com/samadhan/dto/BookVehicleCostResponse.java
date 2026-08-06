package com.samadhan.dto;


public class BookVehicleCostResponse {

    private String vehicle;

    private Double rideCost;

    private Double gst;

    private Double loadingUnloading;

    private Double packaging;

    private Double totalCost;
    
    private Double helperCharge;
    
    private double laborCost;     // NEW
    private double floorCharge; 

	public BookVehicleCostResponse(String vehicle, Double rideCost, Double gst, Double loadingUnloading,
			Double packaging, Double totalCost, Double laborCost, Double floorCharge) {
		this.vehicle = vehicle;
		this.rideCost = rideCost;
		this.gst = gst;
		this.loadingUnloading = loadingUnloading;
		this.packaging = packaging;
		this.totalCost = totalCost;
		this.laborCost = laborCost;
		this.floorCharge = floorCharge;
	}

	public BookVehicleCostResponse() {
		// TODO Auto-generated constructor stub
	}

	public String getVehicle() {
		return vehicle;
	}

	public void setVehicle(String vehicle) {
		this.vehicle = vehicle;
	}

	public Double getRideCost() {
		return rideCost;
	}

	public void setRideCost(Double rideCost) {
		this.rideCost = rideCost;
	}

	public Double getGst() {
		return gst;
	}

	public void setGst(Double gst) {
		this.gst = gst;
	}

	public Double getLoadingUnloading() {
		return loadingUnloading;
	}

	public void setLoadingUnloading(Double loadingUnloading) {
		this.loadingUnloading = loadingUnloading;
	}

	public Double getPackaging() {
		return packaging;
	}

	public void setPackaging(Double packaging) {
		this.packaging = packaging;
	}

	public Double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(Double totalCost) {
		this.totalCost = totalCost;
	}

	public Double getHelperCharge() {
		return helperCharge;
	}

	public void setHelperCharge(Double helperCharge) {
		this.helperCharge = helperCharge;
	}

	public double getLaborCost() {
		return laborCost;
	}

	public void setLaborCost(double laborCost) {
		this.laborCost = laborCost;
	}

	public double getFloorCharge() {
		return floorCharge;
	}

	public void setFloorCharge(double floorCharge) {
		this.floorCharge = floorCharge;
	}
}