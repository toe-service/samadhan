package com.samadhan.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.samadhan.dto.AadhaarVerificationResult;
import com.samadhan.dto.RcVerificationResult;

// Thin wrapper around the Surepass KYC API. NOT wired to a real Surepass contract yet —
// Surepass's marketing pages don't publish endpoint paths, auth header format, or
// request/response fields; those only come from their dashboard docs once we have an account.
// surepass.api.key being blank (the default) is the "not signed up yet" state: verifyAadhaar/
// verifyRc short-circuit to an unverified result instead of making a doomed call, so every
// other layer (entities, controllers, both frontends) can be built and deployed ahead of the
// real credentials. Once we have docs, replace the two throws below with the actual RestTemplate
// call — base URL / key are already config-driven via application*.properties.
@Component
public class SurepassClient {

	private static final Logger logger = LoggerFactory.getLogger(SurepassClient.class);

	@Value("${surepass.api.key:}")
	private String apiKey;

	@Value("${surepass.api.base-url:https://kyc-api.surepass.io}")
	private String baseUrl;

	public boolean isConfigured() {
		return apiKey != null && !apiKey.isBlank();
	}

	public AadhaarVerificationResult verifyAadhaar(String aadhaarNumber) {
		if (!isConfigured()) {
			logger.warn("Surepass API key not configured — skipping Aadhaar verification");
			return AadhaarVerificationResult.unconfigured();
		}
		// TODO: call the real Surepass Aadhaar endpoint (baseUrl + apiKey are ready above).
		throw new UnsupportedOperationException(
				"Surepass Aadhaar endpoint not yet wired — add the real endpoint/fields once dashboard docs are available");
	}

	public RcVerificationResult verifyRc(String vehicleNumber) {
		if (!isConfigured()) {
			logger.warn("Surepass API key not configured — skipping RC verification");
			return RcVerificationResult.unconfigured();
		}
		// TODO: call the real Surepass RC endpoint (baseUrl + apiKey are ready above).
		throw new UnsupportedOperationException(
				"Surepass RC endpoint not yet wired — add the real endpoint/fields once dashboard docs are available");
	}
}
