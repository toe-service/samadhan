package com.samadhan.dto;

public class AadhaarVerificationResult {

	private boolean verified;
	private String name;
	private String dob;
	private String gender;
	private String address;
	private String message;

	public static AadhaarVerificationResult unconfigured() {
		AadhaarVerificationResult result = new AadhaarVerificationResult();
		result.setVerified(false);
		result.setMessage("Aadhaar verification is not configured yet.");
		return result;
	}

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDob() {
		return dob;
	}

	public void setDob(String dob) {
		this.dob = dob;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
