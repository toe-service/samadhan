package com.samadhan.dto;

public class RcVerificationResult {

	private boolean verified;
	private String ownerName;
	private String vehicleClass;
	private String registrationDate;
	private String registrationStatus;
	private String fitnessUpto;
	private String message;

	public static RcVerificationResult unconfigured() {
		RcVerificationResult result = new RcVerificationResult();
		result.setVerified(false);
		result.setMessage("RC verification is not configured yet.");
		return result;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getVehicleClass() {
		return vehicleClass;
	}

	public void setVehicleClass(String vehicleClass) {
		this.vehicleClass = vehicleClass;
	}

	public String getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(String registrationDate) {
		this.registrationDate = registrationDate;
	}

	public String getRegistrationStatus() {
		return registrationStatus;
	}

	public void setRegistrationStatus(String registrationStatus) {
		this.registrationStatus = registrationStatus;
	}

	public String getFitnessUpto() {
		return fitnessUpto;
	}

	public void setFitnessUpto(String fitnessUpto) {
		this.fitnessUpto = fitnessUpto;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
