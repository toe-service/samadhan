package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorWallet;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.repository.VendorWalletRepository;

@Service
public class TransferVendorServiceImpl implements TransferVendorService{

	@Autowired
	TransferVendorRepository transferVendorRepo;
	
	@Autowired
	VendorWalletRepository VendorWalletRepo;
	
	//@Override
//	public TransferVendor registerVendor(TransferVendor transferVendor) {
//		
//		if(transferVendor.getVendorEmail() != null) {
//			
//			String password = transferVendor.getVendorEmail().replace("@gmail.com", "");
//			transferVendor.setVendorPassword(password);
//		}
//		transferVendor.setVendorStatus(VendorStatusEnum.VERIFICATION_PENDING);
//		TransferVendor transferVendorRegister=transferVendorRepo.save(transferVendor);
//		return transferVendorRegister;
//	}

	@Override
	public VendorWallet walletByVendor(Long vendorId) {
		VendorWallet walletByVendor=VendorWalletRepo.findByVendor(vendorId);

		return walletByVendor;
	}

	@Override
	public TransferVendor registerVendor(String vendorName, String vendorEmail, String vendorContactNumber,
			String vendorCity, String vendorAddress, String vendorLatitude, String vendorLongitude,
			MultipartFile aadhaarFile, MultipartFile panFile, String gst) {
		  TransferVendor vendor = new TransferVendor();

			if(vendorEmail != null) {
			
			String password = vendorEmail.replace("@gmail.com", "");
			vendor.setVendorPassword(password);
			}
		 
		    vendor.setVendorName(vendorName);
		    vendor.setVendorEmail(vendorEmail);
		    vendor.setVendorContactNumber(vendorContactNumber);
		    vendor.setVendorCity(vendorCity);
		    vendor.setVendorAddress(vendorAddress);
		    vendor.setVendorStatus(VendorStatusEnum.VERIFICATION_PENDING);
		    vendor.setVendorLatitude(vendorLatitude);
		    vendor.setVendorLongitude(vendorLongitude);
		    vendor.setGstNumber(gst);
		    
		    transferVendorRepo.save(vendor);

		    return vendor;
	}

}
