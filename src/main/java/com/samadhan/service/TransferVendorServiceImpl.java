package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorWallet;
import com.samadhan.entity.WalletTransaction;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.repository.VendorWalletRepository;
import com.samadhan.repository.WalletTransactionRepo;

@Service
public class TransferVendorServiceImpl implements TransferVendorService{

	@Autowired
	TransferVendorRepository transferVendorRepo;
	
	@Autowired
	VendorWalletRepository VendorWalletRepo;
	
	@Autowired
	WalletTransactionRepo walletTransactionRepo; 
	
	@Autowired
	TransferRequestRepository transferRequestRepo;
	
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

	@Transactional
	public void deductLeadCost(Long vendorId, Long requestId) {

	    VendorWallet wallet = VendorWalletRepo
	            .findByVendor(vendorId);

	    if (wallet == null) {
	        throw new RuntimeException(
	                "Wallet not found");
	    }

	    WalletTransaction existing =
	    		walletTransactionRepo
	                    .findByVendorANDRequest(
	                            requestId,
	                            vendorId,"LEAD FEE");

	    if (existing != null) {
	        // Already paid for this lead
	        return;
	    }

	    double leadCost = 20.0;

	    if (wallet.getBalance() < leadCost) {
	        throw new RuntimeException(
	                "Insufficient wallet balance");
	    }

	    wallet.setBalance(
	            wallet.getBalance() - leadCost
	    );

	    VendorWalletRepo.save(wallet);

	    TransferVendor vendor =
	            transferVendorRepo
	                    .findById(vendorId)
	                    .orElseThrow(() ->
	                            new RuntimeException(
	                                    "Vendor not found"));

	    TransferRequestDetails request =
	    		transferRequestRepo
	                    .findById(requestId)
	                    .orElseThrow(() ->
	                            new RuntimeException(
	                                    "Request not found"));

	    WalletTransaction transaction =
	            new WalletTransaction();

	    transaction.setVendor(vendor);
	    transaction.setTransferRequestDetail(request);
	    transaction.setAmount(leadCost);
	    transaction.setTransactionType("LEAD FEE");
	    transaction.setDescription("LEAD_VIEW");

	    walletTransactionRepo.save(transaction);
	}

}
