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

import com.samadhan.dto.PublicVendorProfileDto;
import com.samadhan.dto.StoredImageResponse;
import com.samadhan.dto.WalletTransactionDto;
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
import com.samadhan.exception.NotFoundException;
import com.samadhan.exception.WalletLowBalanceException;
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
	public List<WalletTransactionDto> getWalletTransactions(Long vendorId) {
		return walletTransactionRepo.findByVendor_IdOrderByIdDesc(vendorId)
				.stream()
				.map(t -> {
					WalletTransactionDto dto = new WalletTransactionDto();
					dto.setId(t.getId());
					dto.setAmount(t.getAmount());
					dto.setTransactionType(t.getTransactionType());
					dto.setDescription(t.getDescription());
					dto.setCreatedDate(t.getCreatedDate());
					dto.setTransferRequestId(
							t.getTransferRequestDetail() != null ? t.getTransferRequestDetail().getId() : null);
					return dto;
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public TransferVendor registerVendor(String vendorName, String vendorEmail, String vendorContactNumber,
			String vendorCity, String vendorAddress, String vendorLatitude, String vendorLongitude,
			MultipartFile aadhaarFile, MultipartFile panFile, MultipartFile signatureFile, String gst,
			String services, Boolean isIndividual, Boolean termsAccepted, String termsVersion, String termsText)
			throws ConflictException {

		  boolean hasAadhaar = aadhaarFile != null && !aadhaarFile.isEmpty();
		  boolean hasPan = panFile != null && !panFile.isEmpty();
		  if (!hasAadhaar && !hasPan) {
			  throw new ConflictException("Please upload either Aadhaar Card or PAN Card");
		  }

		  if (signatureFile == null || signatureFile.isEmpty()) {
			  throw new ConflictException("Please upload your signature");
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

		    // Upload Signature
		    if (signatureFile != null && !signatureFile.isEmpty()) {

		        String signatureKey = uploadVendorDocument(
		                vendor.getId(),
		                signatureFile,
		                "signature");

		        vendor.setSignatureStorageKey(signatureKey);
		    }

		    // Save storage keys
		    vendor = transferVendorRepo.save(vendor);
		    
		    
		    
		    
		    VendorWallet wallet = new VendorWallet();
			
			

			wallet.setBalance(0.0);
			wallet.setVendor(vendor);
			
			VendorWalletRepo.save(wallet);

		    return vendor;
	}

	@Override
	public PublicVendorProfileDto getPublicVendorProfile(String vendorSlug) throws NotFoundException {

		TransferVendor vendor = transferVendorRepo.findByVendorNameSlug(vendorSlug);
		if (vendor == null) {
			throw new NotFoundException("Vendor not found");
		}

		if (isPubliclyHidden(vendor)) {
			throw new NotFoundException("Vendor not found");
		}

		PublicVendorProfileDto dto = new PublicVendorProfileDto();
		dto.setVendorName(vendor.getVendorName());
		dto.setVendorCity(vendor.getVendorCity());
		dto.setVendorAddress(vendor.getVendorAddress());
		dto.setVendorContactNumber(vendor.getVendorContactNumber());
		dto.setVendorEmail(vendor.getVendorEmail());
		dto.setIsIndividual(vendor.getIsIndividual());
		dto.setVerified(true);
		dto.setAvgRating(vendor.getAvgRating());
		dto.setRatingCount(vendor.getRatingCount());
		dto.setBusinessTagline(vendor.getBusinessTagline());
		dto.setAboutText(vendor.getAboutText());
		// Proxy URLs, not direct storage links -- the bucket may not be publicly readable, and
		// this keeps the storage key itself out of the public response. Null (not an empty-image
		// link) when the vendor never uploaded one, so the frontend can fall back to a generated
		// placeholder instead of requesting a 404.
		if (vendor.getLogoStorageKey() != null && !vendor.getLogoStorageKey().isBlank()) {
			dto.setLogoUrl("/transferVendor/public-profile/" + vendorSlug + "/logo");
		}
		if (vendor.getCoverImageStorageKey() != null && !vendor.getCoverImageStorageKey().isBlank()) {
			dto.setCoverImageUrl("/transferVendor/public-profile/" + vendorSlug + "/cover");
		}

		List<String> services = vendor.getVendorServices() == null
				? List.of()
				: vendor.getVendorServices().stream()
						.filter(VendorService::isActive)
						.map(vs -> vs.getServiceType().name())
						.collect(Collectors.toList());
		dto.setServices(services);

		return dto;
	}

	// Same cases getPublicVendorProfile already excluded, pulled out so getPublicProfileImage
	// below enforces the identical rule — a lapsed/suspended/rejected vendor's logo or cover
	// photo shouldn't be fetchable even if someone already has the direct image URL cached from
	// before their page went away.
	private boolean isPubliclyHidden(TransferVendor vendor) {
		VendorStatusEnum status = vendor.getVendorStatus();
		return status == VendorStatusEnum.REJECTED
				|| status == VendorStatusEnum.SUSPENDED
				|| status == VendorStatusEnum.SUBSCRIPTION_PENDING;
	}

	private static final int BUSINESS_TAGLINE_MAX_LENGTH = 150;
	private static final int ABOUT_TEXT_MAX_LENGTH = 2000;

	// Vendor-editable public-page copy (TransferVendorController's /profile-content) — separate
	// from registerVendor's one-time KYC fields, this can be changed as often as the vendor likes.
	// Length-capped so the public page's layout can't be blown out by an arbitrarily long paste.
	@Override
	public TransferVendor updateProfileContent(Long vendorId, String businessTagline, String aboutText) {
		TransferVendor vendor = transferVendorRepo.findById(vendorId)
				.orElseThrow(() -> new com.samadhan.exception.ResourceNotFoundException("Vendor not found: " + vendorId));

		if (businessTagline != null && businessTagline.length() > BUSINESS_TAGLINE_MAX_LENGTH) {
			throw new IllegalArgumentException(
					"Business tagline must be " + BUSINESS_TAGLINE_MAX_LENGTH + " characters or fewer");
		}
		if (aboutText != null && aboutText.length() > ABOUT_TEXT_MAX_LENGTH) {
			throw new IllegalArgumentException(
					"About text must be " + ABOUT_TEXT_MAX_LENGTH + " characters or fewer");
		}

		vendor.setBusinessTagline(businessTagline != null && businessTagline.isBlank() ? null : businessTagline);
		vendor.setAboutText(aboutText != null && aboutText.isBlank() ? null : aboutText);
		return transferVendorRepo.save(vendor);
	}

	// Vendor-editable public-page branding image (TransferVendorController's /profile-image) —
	// "logo" or "cover", reuses the same upload mechanics as the KYC documents (uploadVendorDocument
	// below) but under its own folder and with an image-only content-type check, since this one's
	// served back to the public (see getPublicProfileImage) rather than staying private.
	@Override
	public TransferVendor uploadProfileImage(Long vendorId, String type, MultipartFile file) {
		TransferVendor vendor = transferVendorRepo.findById(vendorId)
				.orElseThrow(() -> new com.samadhan.exception.ResourceNotFoundException("Vendor not found: " + vendorId));

		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("No file provided");
		}
		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image/")) {
			throw new IllegalArgumentException("Only image files are allowed");
		}

		String storageKey = uploadVendorDocument(vendorId, file, "branding/" + type);
		if ("cover".equalsIgnoreCase(type)) {
			vendor.setCoverImageStorageKey(storageKey);
		} else {
			vendor.setLogoStorageKey(storageKey);
		}
		return transferVendorRepo.save(vendor);
	}

	private static final java.util.Map<String, String> IMAGE_CONTENT_TYPES_BY_EXTENSION = java.util.Map.of(
			"png", "image/png",
			"jpg", "image/jpeg",
			"jpeg", "image/jpeg",
			"webp", "image/webp",
			"gif", "image/gif",
			"svg", "image/svg+xml"
	);

	// Public, no-auth image proxy backing getPublicVendorProfile's logoUrl/coverImageUrl — streams
	// the stored bytes back rather than redirecting to a direct bucket URL, since the bucket isn't
	// necessarily publicly readable and this keeps the storage key itself out of any response.
	@Override
	public StoredImageResponse getPublicProfileImage(String vendorSlug, String type) throws NotFoundException {
		TransferVendor vendor = transferVendorRepo.findByVendorNameSlug(vendorSlug);
		if (vendor == null || isPubliclyHidden(vendor)) {
			throw new NotFoundException("Vendor not found");
		}

		String storageKey = "cover".equalsIgnoreCase(type)
				? vendor.getCoverImageStorageKey() : vendor.getLogoStorageKey();
		if (storageKey == null || storageKey.isBlank()) {
			throw new NotFoundException("Image not found");
		}

		byte[] data = storageService.getObjectAsBytes(storageKey);
		String extension = storageKey.contains(".")
				? storageKey.substring(storageKey.lastIndexOf('.') + 1).toLowerCase() : "";
		String contentType = IMAGE_CONTENT_TYPES_BY_EXTENSION.getOrDefault(extension, "image/jpeg");
		return new StoredImageResponse(data, contentType);
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

	    if (wallet.getBalance() - leadCost < -200) {
	        throw new WalletLowBalanceException("Low wallet balance. Please recharge your wallet.");
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

}
