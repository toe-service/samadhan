package com.samadhan.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorWallet;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.OtpMismatchException;
import com.samadhan.request.ChangePasswordRequest;
import com.samadhan.request.ForgotPasswordRequest;
import com.samadhan.request.ResetPasswordRequest;
import com.samadhan.response.ResponseObject;
import com.samadhan.security.TokenApi;
import com.samadhan.service.LoginService;
import com.samadhan.service.TransferVendorService;
import com.samadhan.service.VehicleService;
import com.samadhan.util.ResponseUtil;

@RestController
@RequestMapping(value = "/transferVendor")
public class TransferVendorController {

	 @Autowired
	 TransferVendorService transferVendorService;

	 @Autowired
	 LoginService loginService;

	 @Autowired
	 TokenApi tokenApi;

	@PostMapping(value = "/register-vendor")
	public TransferVendor registerVendor(
	        @RequestParam String vendorName,
	        @RequestParam String vendorEmail,
	        @RequestParam String vendorContactNumber,
	        @RequestParam String vendorCity,
	        @RequestParam String vendorAddress,
	        @RequestParam String vendorLatitude,
	        @RequestParam String vendorLongitude,
	        @RequestParam String gst,
	        @RequestParam(required = false) String services,
	        @RequestParam(required = false) MultipartFile aadhaarFile,
	        @RequestParam(required = false) MultipartFile panFile,
	        @RequestParam(required = false) Boolean isIndividual,
	        @RequestParam(required = false) Boolean termsAccepted,
	        @RequestParam(required = false) String termsVersion,
	        @RequestParam(required = false) String termsText
	) throws ConflictException {

		TransferVendor resp = transferVendorService.registerVendor(vendorName, vendorEmail, vendorContactNumber, vendorCity, vendorAddress, vendorLatitude, vendorLongitude, aadhaarFile,panFile, gst, services, isIndividual, termsAccepted, termsVersion, termsText );
		return resp;
	}

	@GetMapping(value = "/wallet-vendor/{vendorId}")
	public VendorWallet walletByVendor(@PathVariable Long vendorId) {
		
		VendorWallet walletByVendor = transferVendorService.walletByVendor(vendorId);
		return walletByVendor;
	}
	
	@PostMapping("/wallet/deduct-lead-cost/{vendorId}/{requestId}/{userType}")
	public ResponseEntity<?> deductLeadCost(
	        @PathVariable Long vendorId,
	        @PathVariable Long requestId,@PathVariable(required = false) String userType) {
		
		transferVendorService.deductLeadCost(vendorId, requestId, userType);

	  //  walletService.deductLeadCost(vendorId, requestId);

	    return ResponseEntity.ok("₹20 deducted successfully");
	}

	// Public — no auth yet at this point, the vendor forgot their password. Always returns the
	// same generic message regardless of whether vendorEmail matches a real vendor, so this
	// can't be used to enumerate registered accounts.
	@PostMapping("/password/forgot")
	public ResponseEntity<ResponseObject<?>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		loginService.requestVendorPasswordReset(request.getVendorEmail());
		ResponseObject<String> success = ResponseUtil.populateResponseObject(
				"If this email is registered, an OTP has been sent to the vendor's registered mobile number.",
				"SUCCESS", null);
		return ResponseEntity.ok(success);
	}

	// Public — pairs with /password/forgot. Verifying the OTP and setting the new password
	// happen atomically here rather than as two separate calls.
	@PostMapping("/password/reset")
	public ResponseEntity<ResponseObject<?>> resetPassword(@RequestBody ResetPasswordRequest request)
			throws OtpMismatchException {
		loginService.resetVendorPassword(request.getVendorEmail(), request.getOtp(), request.getNewPassword());
		ResponseObject<String> success = ResponseUtil.populateResponseObject(
				"Password reset successfully. You can now log in with your new password.",
				"SUCCESS", null);
		return ResponseEntity.ok(success);
	}

	// Authenticated (default security rule — see SecurityConfig). The JWT's own userId claim
	// must match the vendorId being changed, so one vendor's valid token can't be used to change
	// a different vendor's password by editing the path.
	@PostMapping("/password/change/{vendorId}")
	public ResponseEntity<ResponseObject<?>> changePassword(
			@PathVariable Long vendorId,
			@RequestBody ChangePasswordRequest request,
			HttpServletRequest httpRequest) {

		String authHeader = httpRequest.getHeader("Authorization");
		String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
		Long tokenVendorId = jwt != null ? tokenApi.extractUserId(jwt) : null;

		if (tokenVendorId == null || !tokenVendorId.equals(vendorId)) {
			throw new AccessDeniedException("You are not authorized to change this vendor's password");
		}

		loginService.changeVendorPassword(vendorId, request.getCurrentPassword(), request.getNewPassword());
		ResponseObject<String> success = ResponseUtil.populateResponseObject(
				"Password changed successfully.", "SUCCESS", null);
		return ResponseEntity.ok(success);
	}
	
}
