package com.samadhan.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.samadhan.dto.WalletTransactionDto;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorWallet;
import com.samadhan.exception.ConflictException;

public interface TransferVendorService {

	//TransferVendor registerVendor(TransferVendor transferVendor);

	VendorWallet walletByVendor(Long vendorId);

	List<WalletTransactionDto> getWalletTransactions(Long vendorId);

	TransferVendor registerVendor(String vendorName, String vendorEmail, String vendorContactNumber, String vendorCity,
			String vendorAddress, String vendorLatitude, String vendorLongitude, MultipartFile aadhaarFile,
			MultipartFile panFile, String gst, String services, Boolean isIndividual, Boolean termsAccepted,
			String termsVersion, String termsText) throws ConflictException;

	void deductLeadCost(Long vendorId, Long requestId, String userType);

}
