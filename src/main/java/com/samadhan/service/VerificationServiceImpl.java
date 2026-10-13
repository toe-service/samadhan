package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.dto.AadhaarVerificationResult;
import com.samadhan.dto.RcVerificationResult;

@Service
public class VerificationServiceImpl implements VerificationService {

	@Autowired
	private SurepassClient surepassClient;

	@Override
	public AadhaarVerificationResult verifyAadhaar(String aadhaarNumber) {
		if (aadhaarNumber == null || !aadhaarNumber.matches("\\d{12}")) {
			throw new IllegalArgumentException("Aadhaar number must be exactly 12 digits");
		}
		return surepassClient.verifyAadhaar(aadhaarNumber);
	}

	@Override
	public RcVerificationResult verifyRc(String vehicleNumber) {
		if (vehicleNumber == null || vehicleNumber.isBlank()) {
			throw new IllegalArgumentException("Vehicle number is required");
		}
		return surepassClient.verifyRc(vehicleNumber.trim().toUpperCase());
	}
}
