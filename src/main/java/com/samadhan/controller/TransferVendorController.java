package com.samadhan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
import com.samadhan.service.TransferVendorService;
import com.samadhan.service.VehicleService;

@RestController
@RequestMapping(value = "/transferVendor")
public class TransferVendorController {

	 @Autowired
	 TransferVendorService transferVendorService;
	 
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
	
}
