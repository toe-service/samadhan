package com.samadhan.service;

import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorWallet;

public interface TransferVendorService {

	//TransferVendor registerVendor(TransferVendor transferVendor);

	VendorWallet walletByVendor(Long vendorId);

	TransferVendor registerVendor(String vendorName, String vendorEmail, String vendorContactNumber, String vendorCity,
			String vendorAddress, String vendorLatitude, String vendorLongitude, MultipartFile aadhaarFile,
			MultipartFile panFile, String gst);

	void deductLeadCost(Long vendorId, Long requestId, String userType);

}
