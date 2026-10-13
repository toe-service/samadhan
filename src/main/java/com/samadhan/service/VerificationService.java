package com.samadhan.service;

import com.samadhan.dto.AadhaarVerificationResult;
import com.samadhan.dto.RcVerificationResult;

public interface VerificationService {

	AadhaarVerificationResult verifyAadhaar(String aadhaarNumber);

	RcVerificationResult verifyRc(String vehicleNumber);

}
