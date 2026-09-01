package com.samadhan.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.samadhan.entity.Subscription;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorService;
import com.samadhan.entity.VendorWallet;
import com.samadhan.entity.WalletTransaction;
import com.samadhan.enums.PaymentTypeEnum;
import com.samadhan.enums.SubscriptionPeriodEnum;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.exception.ConflictException;
import com.samadhan.repository.PaymentRepository;
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

	@Autowired
	PaymentRepository paymentRepo;

	@Autowired
	PasswordEncoder passwordEncoder;

	// Every new vendor starts here automatically — no button, no payment — matching the "trial
	// starts on registration" flow rather than the old manual, paid "Free Subscription" button.
	private static final int TRIAL_DAYS = 15;

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
			MultipartFile aadhaarFile, MultipartFile panFile, String gst, String services, Boolean isIndividual,
			Boolean termsAccepted, String termsVersion, String termsText)
			throws ConflictException {

		  boolean hasAadhaar = aadhaarFile != null && !aadhaarFile.isEmpty();
		  boolean hasPan = panFile != null && !panFile.isEmpty();
		  if (!hasAadhaar && !hasPan) {
			  throw new ConflictException("Please upload either Aadhaar Card or PAN Card");
		  }

		  if (termsAccepted == null || !termsAccepted) {
			  throw new ConflictException("You must accept the Terms & Conditions to register");
		  }

		  // Nothing enforced this before, which is how duplicate vendor_email rows ended up in
		  // the database — findByVendorEmail (login, forgot-password) then throws
		  // NonUniqueResultException the moment 2+ rows share an email.
		  if (vendorEmail != null && transferVendorRepo.findByVendorEmail(vendorEmail) != null) {
			  throw new ConflictException("A vendor is already registered with this email");
		  }

		  TransferVendor vendor = new TransferVendor();

			if(vendorEmail != null) {

			// Generalizes the old "@gmail.com"-only stripping (which left the FULL email as
			// the password for any non-Gmail address) to any domain: local-part before '@'.
			// Hashed before storage — see LoginService for the matching verification side and
			// why it also transparently upgrades any pre-existing plaintext passwords.
			int atIndex = vendorEmail.indexOf('@');
			String password = atIndex > 0 ? vendorEmail.substring(0, atIndex) : vendorEmail;
			vendor.setVendorPassword(passwordEncoder.encode(password));
			}
		 
		    vendor.setVendorName(vendorName);
		    vendor.setVendorEmail(vendorEmail);
		    vendor.setVendorContactNumber(vendorContactNumber);
		    vendor.setVendorCity(vendorCity);
		    vendor.setVendorAddress(vendorAddress);
		    // Vendor gets full access immediately via the 15-day trial created below — there's
		    // no separate admin-verification step in this codebase today (VERIFICATION_PENDING
		    // was previously only ever cleared by the old manual/paid "Free Subscription"
		    // button, confirmed by grepping for any other place it's read or transitioned).
		    vendor.setVendorStatus(VendorStatusEnum.Free_SUBSCRIPTION);
		    vendor.setVendorLatitude(vendorLatitude);
		    vendor.setVendorLongitude(vendorLongitude);
		    vendor.setGstNumber(gst);
		    vendor.setIsIndividual(isIndividual != null && isIndividual);
		    // Server-set timestamp, not the client's — a client-supplied clock isn't trustworthy
		    // as evidence of when acceptance actually happened.
		    vendor.setTermsAccepted(true);
		    vendor.setTermsAcceptedAt(LocalDateTime.now());
		    vendor.setTermsVersion(termsVersion);
		    vendor.setTermsText(termsText);
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

		    // Automatic 15-day trial subscription — no button, no payment.
		    Subscription trial = new Subscription();
		    trial.setVendor(vendor);
		    trial.setSubscriptionPeriod(SubscriptionPeriodEnum.TRIAL);
		    trial.setPaymentType(PaymentTypeEnum.TRIAL);
		    trial.setStartDate(LocalDate.now());
		    trial.setEndDate(LocalDate.now().plusDays(TRIAL_DAYS));
		    paymentRepo.save(trial);

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

	  if(userType!=null && (userType.equalsIgnoreCase("User") || userType.equalsIgnoreCase("WebUser"))) {
		
		
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
