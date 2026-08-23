package com.samadhan.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorService;
import com.samadhan.entity.VendorWallet;
import com.samadhan.entity.WalletTransaction;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.repository.TransferMediaRepository;
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
	
	private final StorageService storageService;
	
	  public TransferVendorServiceImpl( StorageService storageService) {
	      this.storageService = storageService;
	    }
	
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
	@Transactional
	public TransferVendor registerVendor(String vendorName, String vendorEmail, String vendorContactNumber,
			String vendorCity, String vendorAddress, String vendorLatitude, String vendorLongitude,
			MultipartFile aadhaarFile, MultipartFile panFile, String gst, String services) {
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
		    final TransferVendor finalVendor = vendor;
		    if (services != null && !services.isBlank()) {
		        List<VendorService> vendorServiceList = Arrays.stream(services.split(","))
		                .map(String::trim)
		                .filter(s -> !s.isEmpty())
		                .map(s -> {
		                    VendorService vs = new VendorService();
		                    vs.setServiceType(serviceTypeEnum.valueOf(s));
		                    vs.setActive(true);
		                    vs.setTransferVendor(finalVendor); // sets the FK side
		                    return vs;
		                })
		                .collect(Collectors.toList());

		        vendor.setVendorServices(vendorServiceList);
		    }
		    
		    transferVendorRepo.save(vendor);
		    
		    
		    
		 // Upload Aadhaar
		    if (aadhaarFile != null && !aadhaarFile.isEmpty()) {

		        String aadhaarKey = uploadVendorDocument(
		                vendor.getId(),
		                aadhaarFile,
		                "aadhaar");

		        vendor.setAadhaarStorageKey(aadhaarKey);
		    }

		    // Upload PAN
		    if (panFile != null && !panFile.isEmpty()) {

		        String panKey = uploadVendorDocument(
		                vendor.getId(),
		                panFile,
		                "pan");

		        vendor.setPanStorageKey(panKey);
		    }

		    // Save storage keys
		    vendor = transferVendorRepo.save(vendor);
		    
		    
		    
		    
		    VendorWallet wallet = new VendorWallet();
			
			

			wallet.setBalance(0.0);
			wallet.setVendor(vendor);
			
			VendorWalletRepo.save(wallet);

		    return vendor;
	}

	private String uploadVendorDocument(Long vendorId, MultipartFile file, String folderName) {

		if (file == null || file.isEmpty()) {
			return null;
		}

		String originalFilename = file.getOriginalFilename();

		String storageKey = String.format("transfer-vendors/%d/%s/%d_%s", vendorId, folderName, System.currentTimeMillis(),
				originalFilename);

		try {

			storageService.uploadFile(storageKey, file.getInputStream(), file.getSize(), file.getContentType());

			return storageKey;

		} catch (Exception e) {
			throw new RuntimeException("Failed to upload " + folderName, e);
		}
	}

	@Transactional
	public void deductLeadCost(Long vendorId, Long requestId, String userType) {

	  if(userType!=null && userType.equalsIgnoreCase("User")) {
		
		
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

//	    if (wallet.getBalance() < leadCost) {
//	        throw new RuntimeException(
//	                "Insufficient wallet balance");
//	    }

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

}
