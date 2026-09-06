package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.security.SecureRandom;

import javax.transaction.Transactional;

import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.DimensionUnit;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.exception.ResourceNotFoundException;
import com.samadhan.exception.SubscriptionSuspendedException;
import com.samadhan.exception.WalletLowBalanceException;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Objects;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.samadhan.entity.CancelledRequest;
import com.samadhan.entity.Driver;
import com.samadhan.entity.ParcelDetails;
import com.samadhan.entity.Ride;
import com.samadhan.entity.Subscription;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.entity.VendorWallet;
import com.samadhan.entity.WalletTransaction;
import com.samadhan.repository.*;
import com.samadhan.util.FireBaseMessagingService;



@Component
public class TransferRequestServiceImpl implements TransferRequestService{

	@Autowired
	TransferRequestRepository transferRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	DriverRepository driverRepo;
	
	@Autowired
	VehicleRepository vehicleRepo;
	
	@Autowired
	TransferVendorRepository transferVendorRepo;
	
	@Autowired
	CancelledRequestRepository CancelledRequestRepo;
	
	@Autowired
	VendorWalletRepository walletRepository;
	
	@Autowired
	WalletTransactionRepo walletTransactionRepo;
	
	 @Autowired
	 PaymentRepository paymentRepo;
	 
	 @Autowired
	 private FireBaseMessagingService fireBaseMessagingService;
	
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	//private static final Logger logger = LoggerFactory.logger(TransferRequestService.class);
	
	@Override
//	public TransferRequestDetails requestRideTransfer(int vehicleType, int vehicleModel,  String pickuplatitude, String pickuplongitude,
//			String destinationlatitude, String destinationlongitude, Long userId, double rideCost,LocalDate pickupDate, String pickupSchedule,String source, String destination) {
	
		public TransferRequestDetails requestRideTransfer(ParcelTypeEnum parcelType, CarModelEnum carModel,
				String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude,
				Long userId, double rideCost, LocalDate pickupDate, String pickupSchedule, String source,
				String destination, String carNumber, BikeModelEnum bikeModel, String bikeNumber, Double packageWeight,
				String packageDescription, Long vendorId, String userType, String userName, String userContact, Double gstCost, Double rideWithoutTaxCalculation,
				Double loadingUnloading, Double packagingCost, DimensionUnit dimensionUnit, Double length, Double width, Double heigth, serviceTypeEnum serviceType,
				VendorPickupVehicleEnum vendorPickupVehicle, Boolean helperRequired, Integer helperCount, VehicleCategoryEnum vehicleCategory,  Boolean instantBooking,
				String homeType, String packingType, String goodsType, Integer fromFloor, Boolean liftAvailable, String receiverName, String receiverContact, Double distanceInKm,
				Boolean isMovable) throws FirebaseMessagingException {
		
		
		TransferVendor vendor = null;
		TransferRequestDetails transferRequest=new TransferRequestDetails();
		
		if(serviceType == serviceTypeEnum.BOOKVEHICLE &&
				"Vendor".equalsIgnoreCase(userType)) {

			throw new ResourceNotFoundException("Vendor cannot create Book Vehicle request.");
		}
		
		if(vendorId != null){
		    vendor = transferVendorRepo.findById(vendorId)
		            .orElseThrow(() -> new ResourceNotFoundException(
		                    "Vendor not found with id: " + vendorId));
		    
		  if(vendor.getVendorStatus().name().equals("SUSPENDED")) {
			  throw new SubscriptionSuspendedException("Your subscription is suspended. Please contact support or renew your subscription.");
		  }else if(vendor.getVendorStatus().name().equals("SUBSCRIPTION_PENDING")) {
			  throw new SubscriptionSuspendedException("Your free subscription Period is over. Buy your subscription.");
		  }
		}
		
		UserDetails user=null;
		if (userId != null) {
		 user = userRepo.findById(userId)
		            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
		
		}
		ParcelDetails parcelDetails=null;
		if(serviceType.getType().equalsIgnoreCase("TRANSFER_SERVICE")){
		parcelDetails=new ParcelDetails();
		parcelDetails.setParcelType(parcelType);
		
		
		if(parcelType.getType().equalsIgnoreCase("Car")) {

		packageWeight = carModel.getAverageWeightKg();
		parcelDetails.setCarModel(carModel);
		parcelDetails.setParcelWeight(packageWeight);
		parcelDetails.setCarNumber(carNumber);
		parcelDetails.setIsMovable(isMovable != null ? isMovable : true);
		}

		if(parcelType.getType().equalsIgnoreCase("Bike")) {
		packageWeight = bikeModel.getAverageWeightKg();
		parcelDetails.setBikeModel(bikeModel);
		parcelDetails.setParcelWeight(packageWeight);
		parcelDetails.setBikeNumber(bikeNumber);
		parcelDetails.setIsMovable(isMovable != null ? isMovable : true);
		}
		
		if(parcelType.getType().equalsIgnoreCase("Package")) {
		parcelDetails.setParcelWeight(packageWeight);
		parcelDetails.setPackageDescription(packageDescription);
		parcelDetails.setLength(length);
		parcelDetails.setWidth(width);
		parcelDetails.setHeight(heigth);
		parcelDetails.setDimensionUnit(dimensionUnit);
		}
		transferRequest.setParcelDetails(parcelDetails);
		transferRequest.setPackagingCost(packagingCost);
	} else if (serviceType.getType().equalsIgnoreCase("BOOK_VEHICLE")) {
		transferRequest.setVendorPickupVehicle(vendorPickupVehicle);
		 transferRequest.setGoodsType(goodsType);
	
		transferRequest.setHelperRequired(
	            helperRequired != null ? helperRequired : false);

	    transferRequest.setHelperCount(
	            Boolean.TRUE.equals(helperRequired)
	                    ? helperCount
	                    : 0);

	    transferRequest.setVehicleCategory(vehicleCategory);
	    
//	    transferRequest.setInstantBooking(
//	            instantBooking != null ? instantBooking : false);
	}else if (serviceType == serviceTypeEnum.HOMESHIFTING) {
		transferRequest.setVendorPickupVehicle(vendorPickupVehicle);
		
		transferRequest.setHelperRequired(
	            helperRequired != null ? helperRequired : false);

	    transferRequest.setHelperCount(
	            Boolean.TRUE.equals(helperRequired)
	                    ? helperCount
	                    : 0);

	    transferRequest.setVehicleCategory(vehicleCategory);
	    
//	    transferRequest.setInstantBooking(
//	            instantBooking != null ? instantBooking : false);

	    transferRequest.setHomeType(homeType);
	    transferRequest.setPackingType(packingType);
	    transferRequest.setFromFloor(fromFloor);
	    transferRequest.setLiftAvailable(liftAvailable);
	    transferRequest.setPackagingCost(packagingCost);

	}
		
	
//		transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleType]);
	//	transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleModel]);
		
		  transferRequest.setInstantBooking(
		            instantBooking != null ? instantBooking : false);
		  
		  
	
		LocalDate currentDate=LocalDate.now();
		LocalTime cuurentTime=LocalTime.now();
		 if(instantBooking != null && instantBooking==true) {
			 transferRequest.setPickupDate(currentDate);  
		  }else {
			  transferRequest.setPickupDate(pickupDate);  
		  }
		LocalDateTime currentDateTime=LocalDateTime.now();
	//	transferRequest.setPickupDate(pickupDate);
		// Backend is authoritative on this label, not the client: instantBooking already
		// drives pickupDate above, so pickupSchedule must agree rather than rely on whatever
		// string the caller happens to pass.
		transferRequest.setPickupSchedule(
				(instantBooking != null && instantBooking) ? "Immediate" : pickupSchedule);
		transferRequest.setRideCost(rideCost);
		transferRequest.setUserDetails(user);
		transferRequest.setRequestCreatedDate(currentDateTime);
		transferRequest.setDestinationLatitude(destinationlatitude);
		transferRequest.setDestinationLongitude(destinationlongitude);
		transferRequest.setSourceLatitude(pickuplatitude);
		transferRequest.setSourceLongitude(pickuplongitude);
		transferRequest.setSource(source);
		transferRequest.setDestination(destination);
		transferRequest.setGstCost(gstCost);
		transferRequest.setLoadingUnloading(loadingUnloading);
		transferRequest.setRideWithoutTaxCalculation(rideWithoutTaxCalculation);
		transferRequest.setServiceType(serviceType);
		transferRequest.setReceiverName(receiverName);
		transferRequest.setReceiverNumber(receiverContact);
		transferRequest.setDistanceKm(distanceInKm);
		
		//transferRequest.setDimensionUnit(dimensionUnit);
		
		//transferRequest.setTransferCalculation(rideCost);
		if(userType!=null && userType.equalsIgnoreCase("Vendor")){
//			TransferVendor vendor = null;
			UserDetails userDetail = userRepo
			        .findByUserContactNumber(userContact)
			        .orElse(null);
			       

			if(userDetail == null){
			    userDetail = new UserDetails();
			    userDetail.setUserName(userName);
			    userDetail.setUserContactNumber(userContact);

			    userDetail = userRepo.save(userDetail);
			}

			

//			if(vendorId != null){
//			    vendor = transferVendorRepo.findById(vendorId)
//			            .orElseThrow(() -> new ResourceNotFoundException(
//			                    "Vendor not found with id: " + vendorId));
//			}
			transferRequest.setUserDetails(userDetail);
			transferRequest.setTransferVendor(vendor);
			transferRequest.setTransferStatus(rideStatusEnum.ACCEPTED);
			transferRequest.setRequestApprovalDate(currentDateTime);
			transferRequest.setUserType(userType);
		}else if(userType!=null && userType.equalsIgnoreCase("WebUser")) {
			UserDetails userDetail = userRepo
			        .findByUserContactNumber(userContact)
			        .orElse(null);
			       

			if(userDetail == null){
			    userDetail = new UserDetails();
			    userDetail.setUserName(userName);
			    userDetail.setUserContactNumber(userContact);

			    userDetail = userRepo.save(userDetail);
			}
			
			transferRequest.setUserDetails(userDetail);
			transferRequest.setTransferStatus(rideStatusEnum.PENDING);
			transferRequest.setUserType(userType);
			
		}else {
		transferRequest.setUserType(userType);
		transferRequest.setTransferStatus(rideStatusEnum.PENDING);
		}
		
		if(vendorPickupVehicle != null) {
			transferRequest.setVendorPickupVehicle(vendorPickupVehicle);
		}
		
		transferRepo.save(transferRequest);
		
		fireBaseMessagingService.notifyVehicles(transferRequest);
		
//		List<TransferVendor> vendors = transferVendorRepo.findAllActiveVendors();
//		
//		for (TransferVendor vendorr : vendors) {
//
//		    List<TransferRequestDetails> rides =
//		    		transferRepo.showRidestoVendors(vendorr.getId());
//
//		    // If the newly created request is visible for this vendor,
//		    // send notification to that vendor
////		    boolean matched = rides.stream()
////		            .anyMatch(r -> r.getId().equals(savedRequest.getId()));
////
////		    if (matched) {
////		        fireBaseMessagingService.sendVendorNotification(
////		                vendor.getFcmToken(),
////		                savedRequest);
////		    }
//		}
		
		
		return transferRequest;
	}

	@Override
	public List<TransferRequestDetails> getTransferRidesByuser(Long userId) {
		
		List<TransferRequestDetails> transferRidesByUserId = transferRepo.findTransferRideByUserId(userId);
		System.out.println("TransferRidesByUserId" + transferRidesByUserId);
		
		
		return transferRidesByUserId;
		
	}

	@Override
	public TransferRequestDetails requestTransferApproval(Long transferId, int transferApproval, Long vendorId, String cancellationReason,String userType, serviceTypeEnum serviceType, Integer vehicleId, String acceptedBy) {

		LocalDateTime dateTime = LocalDateTime.now();
		


		TransferRequestDetails transferdetails = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
		
		TransferVendor existingVendor=transferdetails.getTransferVendor();

		// Cancelling (transferApproval==3) is expected to run on a request that already has a
		// vendor attached — that's exactly what "already accepted" means. The guard only makes
		// sense for accept(1)/decline(2), otherwise cancel could never reach its own logic below.
		if (existingVendor != null && transferApproval != 3) {
		    throw new RuntimeException("This request is already accepted.");
		}

		TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

		// Gate on wallet balance, not subscription status — a vendor with a low/negative wallet
		// can't take on new rides, but can still decline(2)/cancel(3) work they're already
		// holding. Only checked here for the general case; the User/WebUser branch just below
		// does its own wallet lookup already (tied to the acceptance-fee deduction), so this
		// only needs to additionally cover accept paths that branch doesn't run for.
		if (transferApproval == 1) {
			VendorWallet vendorWalletForGate = walletRepository.findByVendor(vendorId);
			if (vendorWalletForGate != null && vendorWalletForGate.getBalance() < -200) {
				throw new WalletLowBalanceException("Insufficient wallet balance. Please recharge.");
			}
		}

		// The acceptance fee is only charged when a specific vehicle is actually being
		// committed right now — same condition as the vehicle-assignment branch below.
		// A vendor's own generic accept (TRANSFER_SERVICE + acceptedBy=="Vendor", no vehicle
		// picked yet) is free; assigning a vehicle afterwards via requestTransferUpdate is
		// also free (it only gates on balance, never deducts) — the fee only applies once a
		// vehicle itself accepts (acceptedBy=="Vehicle"), or for service types where a vehicle
		// is always attached at accept time (BOOK_VEHICLE / HOME_SHIFTING).
		boolean vehicleCommittedNow = transferdetails.getServiceType() != null && (
				transferdetails.getServiceType().getType().equals("BOOK_VEHICLE")
				|| transferdetails.getServiceType().getType().equalsIgnoreCase("HOME SHIFTING")
				|| (transferdetails.getServiceType().getType().equalsIgnoreCase("TRANSFER_SERVICE")
						&& "Vehicle".equalsIgnoreCase(acceptedBy))
		);

		if(transferApproval==1 && vehicleCommittedNow && (userType !=null && (userType.equalsIgnoreCase("User") || userType.equalsIgnoreCase("WebUser")))) {
		VendorWallet wallet = walletRepository.findByVendor(vendorId);

		double acceptanceFee = calculateAcceptanceFee(transferdetails);
		
		if(wallet.getBalance() < -200){
		     throw new WalletLowBalanceException("Insufficient wallet balance. Please recharge.");
		}

		wallet.setBalance(
			    wallet.getBalance() - acceptanceFee
			);
		
		walletRepository.save(wallet);
		
		WalletTransaction walletTransaction=new WalletTransaction();
		walletTransaction.setAmount(acceptanceFee);
		walletTransaction.setTransactionType("Ride Acceptance Fee");
		walletTransaction.setVendor(transferVendor);
		walletTransaction.setTransferRequestDetail(transferdetails);
		
		walletTransactionRepo.save(walletTransaction);
		}
		
		if(transferApproval==2) {
			
			CancelledRequest cancelRequest=new CancelledRequest();
			cancelRequest.setTransferVendor(transferVendor);
			cancelRequest.setTransferRequest(transferdetails);
			cancelRequest.setcancellationReason(cancellationReason);
			cancelRequest.setRequestFlag(true);
			CancelledRequestRepo.save(cancelRequest);
			return transferdetails;
		}if(transferApproval==3) {
			
			CancelledRequest cancelRequest=new CancelledRequest();
			cancelRequest.setTransferVendor(transferVendor);
			cancelRequest.setTransferRequest(transferdetails);
			cancelRequest.setcancellationReason(cancellationReason);
			cancelRequest.setRequestFlag(false);
			CancelledRequestRepo.save(cancelRequest);
			transferdetails.setTransferStatus(rideStatusEnum.PENDING);
			transferdetails.setRequestApprovalDate(null);
			transferdetails.setTransferVendor(null);
			transferdetails.setVehicleId(null);
			transferdetails.setVehicleAssignDateTime(null);
			transferdetails.setOtp(null);
			transferRepo.save(transferdetails);

			try {
				fireBaseMessagingService.notifyVehicles(transferdetails);
			} catch (Exception e) {
				System.out.println("Failed to re-notify vehicles after cancelling request " + transferId + ": " + e.getMessage());
			}

			return transferdetails;
		}
		else {
			
		if (transferdetails.getServiceType().getType().equals("BOOK_VEHICLE") || transferdetails.getServiceType().getType().equalsIgnoreCase("HOME SHIFTING") || (transferdetails.getServiceType().getType().equalsIgnoreCase("TRANSFER_SERVICE") && acceptedBy.equalsIgnoreCase("Vehicle"))) {
			 Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		              .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			 int otp = 1000 + SECURE_RANDOM.nextInt(9000);
			 transferdetails.setVehicleId(vehicle);
			 transferdetails.setVehicleAssignDateTime(dateTime);
			 transferdetails.setRequestApprovalDate(dateTime);
			 transferdetails.setOtp(otp);
			 transferdetails.setTransferStatus(rideStatusEnum.VEHICLEASSIGNED);
			 transferdetails.setTransferVendor(transferVendor);
		
			
		}else if (transferdetails.getServiceType().getType().equalsIgnoreCase("TRANSFER_SERVICE")  && acceptedBy.equalsIgnoreCase("Vendor")) {

		transferdetails.setTransferStatus(rideStatusEnum.values()[transferApproval]);
		transferdetails.setRequestApprovalDate(dateTime);
		transferdetails.setTransferVendor(transferVendor);
		
		}

		transferRepo.save(transferdetails);
		
		 // 👇 New: tell every other vehicle's app to stop ringing
//	    try {
//	        fireBaseMessagingService.notifyRideTaken(transferdetails);
//	    } catch (Exception e) {
//	       System.out.println("Failed to broadcast RIDE_TAKEN for transfer {}");
//	    }

		return transferdetails;
		}
	}

//	@Override
//	public TransferRequestDetails getRidesByTransferId(Long transferId) {
//		Optional<TransferRequestDetails> transferRidesByUserIdopt = transferRepo.findById(transferId);
//		TransferRequestDetails transferRidesByUserId=transferRidesByUserIdopt.get();
//		System.out.println("TransferRidesByUserId" + transferRidesByUserId);
//		return transferRidesByUserId;
//	}

	private double calculateAcceptanceFee(TransferRequestDetails transferdetails) {

		double rideCost=transferdetails.getRideCost();
		
		return Math.round(rideCost * 0.020 * 100.0) / 100.0;
	}
	
	private double calculateCompletioneFee(TransferRequestDetails transferdetails) {

		double rideCost=transferdetails.getRideCost();
		
		return Math.round(rideCost * 0.040 * 100.0) / 100.0;
	}

	@Override
	public TransferRequestDetails getRidesByTransferId(Long transferId) {

		return transferRepo.findById(transferId)
		        .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
	}

	@Override
	@Transactional
	public TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId, Integer vehicleId,
			Integer rideStatus, String userType) {

		TransferRequestDetails transfer = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));

		LocalDateTime dateTime = LocalDateTime.now();
		int otp = 1000 + SECURE_RANDOM.nextInt(9000);

		//Agent Assignment
		if (driverId != null) {
			Driver driver = driverRepo.findById(driverId)
					.orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

			transfer.setDriver(driver);
			transfer.setDriverAssignDateTime(dateTime);
			// Stamp the actual pickup time here: this is when a driver is assigned and the
			// request becomes READYFORPICKUP, i.e. when the driver has picked up the order.
			// Previously pickup_time was never populated for any booking (immediate or scheduled).
			transfer.setPickupTime(dateTime.toLocalTime());
			transfer.setOtp(otp);
			transfer.setTransferStatus(rideStatusEnum.READYFORPICKUP);
			transferRepo.save(transfer);
		}

		// 🔹 Vehicle Assignment
		if (vehicleId != null && vehicleId != 0) {
//			long vehiId =(long) transfer.getVehicleId();
//			Optional<Vehicle> vehicleopt=vehicleRepo.findById(vehicleId);
			if (userType != null && (userType.equalsIgnoreCase("User") || userType.equalsIgnoreCase("WebUser"))) {
				long vendorId = transfer.getTransferVendor().getId();

				VendorWallet wallet = walletRepository.findByVendor(vendorId);

				if (wallet.getBalance() < -200) {
					throw new WalletLowBalanceException("Insufficient wallet balance. Please recharge.");
				}
			}
			
			
			 Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			transfer.setVehicleId(vehicle);
			transfer.setVehicleAssignDateTime(dateTime);
			transfer.setOtp(otp);
			transfer.setTransferStatus(rideStatusEnum.VEHICLEASSIGNED);
			transferRepo.save(transfer);
		}
 
		//Ride start
		if (rideStatus != null && rideStatus == 0) {
			//long vehiId = (long) vehicleId;
			 Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			
		//	vehicle.setOngoingStatus(true);
			vehicleRepo.save(vehicle);
			
			transfer.setRidestartTime(dateTime);
			transfer.setClosureotp(otp);
			
			transfer.setTransferStatus(rideStatusEnum.ONGOING);
			transferRepo.save(transfer);
			
			if(userType!=null && (userType.equalsIgnoreCase("User") || userType.equalsIgnoreCase("WebUser"))) {

			long vendorId=transfer.getTransferVendor().getId();

			TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
					.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));


			VendorWallet wallet = walletRepository.findByVendor(vendorId);

			double acceptanceFee = calculateAcceptanceFee(transfer);

//			if(wallet.getBalance() < -200){
//			    throw new RuntimeException(
//			        "Insufficient wallet balance. Please recharge."
//			    );
//			}

			wallet.setBalance(
				    wallet.getBalance() - acceptanceFee
				);

			walletRepository.save(wallet);

			WalletTransaction walletTransaction=new WalletTransaction();
			walletTransaction.setAmount(acceptanceFee);
			walletTransaction.setTransactionType("Ride Start Fee");
			walletTransaction.setVendor(transferVendor);
			walletTransaction.setTransferRequestDetail(transfer);
			
			walletTransactionRepo.save(walletTransaction);
			
			}

			
			
		} else if (rideStatus != null && rideStatus == 1) {
			Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			
			vehicle.setOngoingStatus(true);
			vehicleRepo.save(vehicle);
			transfer.setRideendTime(dateTime);
			transfer.setVehicleLastLocation(vehicle.getCurrentLocation());
			transfer.setTransferStatus(rideStatusEnum.COMPLETED);
			transferRepo.save(transfer);
		}

//		transferRepo.save(transfer);

		return transfer;

	}

	@Override
	public boolean otpVerify(Long transferId, int otp, boolean flag, String userType) {

		  TransferRequestDetails transferdetails = transferRepo.findById(transferId)
		            .orElseThrow(() -> new ResourceNotFoundException(
		                    "Transfer not found with id: " + transferId));
		  
		  LocalDateTime dateTime=LocalDateTime.now();
		//if(transferdetails.getOtp()==otp) {
			
//			transferdetails.setHandoveredDateTime(dateTime);

			if(flag) {
			if(transferdetails.getClosureotp() !=null && transferdetails.getClosureotp()==otp) {
			transferdetails.setRideendTime(dateTime);
			if (transferdetails.getVehicleId() != null) {
				transferdetails.setVehicleLastLocation(transferdetails.getVehicleId().getCurrentLocation());
			}
			transferdetails.setTransferStatus(rideStatusEnum.COMPLETED);
			transferRepo.save(transferdetails);
			
			if(userType!=null && (userType.equalsIgnoreCase("User") || userType.equalsIgnoreCase("WebUser"))) {

			long vendorId=transferdetails.getTransferVendor().getId();

			VendorWallet wallet = walletRepository.findByVendor(vendorId);

			TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
					.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

			double acceptanceFee = calculateCompletioneFee(transferdetails);

//			if(wallet.getBalance() < -100){
//			    throw new RuntimeException(
//			        "Insufficient wallet balance. Please recharge."
//			    );
//			}

			wallet.setBalance(
				    wallet.getBalance() - acceptanceFee
				);

			walletRepository.save(wallet);

			WalletTransaction walletTransaction=new WalletTransaction();
			walletTransaction.setAmount(acceptanceFee);
			walletTransaction.setTransactionType("Ride Completion Fee");
			walletTransaction.setVendor(transferVendor);
			walletTransaction.setTransferRequestDetail(transferdetails);
			
			walletTransactionRepo.save(walletTransaction);
			
			}

			return true;
				}
			}else {
				if(transferdetails.getOtp() !=null && transferdetails.getOtp()==otp) {
			transferdetails.setHandoveredDateTime(dateTime);
			transferdetails.setTransferStatus(rideStatusEnum.HANDOVER);
			transferRepo.save(transferdetails);
			return true;
				}
			}
//			transferRepo.save(transferdetails);
//			return true;
		//}

		return false;
	}

	@Override
	public List<TransferRequestDetails> getRidesByDriverId(Long driverId) {
		List<TransferRequestDetails> transferRidesByDriverId = transferRepo.findTransferRideByDriverId(driverId);
		System.out.println("transferRidesByDriverId" + transferRidesByDriverId);
		
		return transferRidesByDriverId;
	}

	@Override
	public List<TransferRequestDetails> showRidestoVendors(Long transferId) {
		TransferVendor vendorForFeed = transferVendorRepo.findById(transferId).orElse(null);
//		if (vendorForFeed != null && vendorForFeed.getVendorStatus() != null
//				&& vendorForFeed.getVendorStatus().name().equals("SUSPENDED")) {
//			throw new SubscriptionSuspendedException(
//					"Your subscription is suspended. Please contact support or renew your subscription.");
//		}

		// An individual account is a single owner-operator vehicle, not a fleet — so their feed
		// should be exactly what their one vehicle would see (vehicle type/location/eligibility
		// matched, see getrideTransferByVehicle), not the vendor-level feed below, which is built
		// around a vendor's business address/radius that individual accounts often don't have set.
		if (vendorForFeed != null && Boolean.TRUE.equals(vendorForFeed.getIsIndividual())) {
			List<Vehicle> vendorVehicles = vehicleRepo.findByVendorId(transferId);
			if (vendorVehicles.isEmpty()) {
				return new ArrayList<>();
			}
			return getrideTransferByVehicle(vendorVehicles.get(0).getId());
		}

		List<TransferRequestDetails> showRidestoVendors = transferRepo.showRidestoVendors(transferId);
		
//		List<TransferRequestDetails> request =
//			    showRidestoVendors.stream()
//			        .filter(req -> req.getTransferStatus() == rideStatusEnum.PENDING)
//			        .collect(Collectors.toList());
		//VendorWallet wallet=walletRepository.findByVendor(transferId);
	 //	for(TransferRequestDetails req : request) {
	 		
	 		
//			 double leadCost=0.0;
//		 	if (wallet == null) {
//		        throw new RuntimeException("Wallet not found");
//		    }
		 	
//		 	 if (wallet.getBalance() < -200) {
//			        throw new RuntimeException("Balance Less than 500");
//		    }
	 		
	 //	WalletTransaction walletTransaction=walletTransactionRepo.findByVendorANDRequest(req.getId(),transferId);
	// 	Optional<TransferVendor> vendor=transferVendorRepo.findById(transferId);
	    // leadCost = 20; 
//	     if (wallet.getBalance() < leadCost) {
//		        throw new RuntimeException("Insufficient wallet balance");
//		    }
	    

	   
//	    if (walletTransaction == null) {
////		    wallet.setBalance(wallet.getBalance() - leadCost);
////	        walletRepository.save(wallet);
//			
//	    
//	    WalletTransaction transaction = new WalletTransaction();
//        transaction.setVendor(vendor.get());
//        transaction.setAmount(20.0);
//        transaction.setTransactionType("DEBIT");
//        transaction.setDescription("LEAD_VIEW");
//        transaction.setTransferRequestDetail(req);
//        walletTransactionRepo.save(transaction);
//	    }
	    
	 	//}
	 	 
	 	

		return showRidestoVendors;
	}

	@Override
	public List<TransferRequestDetails> getrideTransferByVehicle(Long vehicleId) {
		 Vehicle vehicle = vehicleRepo.findById(vehicleId)
		            .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

//		    String vehicleLatitude = vehicle.getVehicleLatitude();
//		    String vehicleLongitude = vehicle.getVehicleLongitude();
//
//		    if (vehicleLatitude == null || vehicleLongitude == null) {
//		        throw new IllegalStateException("Vehicle location not available for id: " + vehicleId);
//		    }

		List<TransferRequestDetails> getRidesByVehicle = transferRepo.getVehicleFeed(vehicleId);
		System.out.println("getRidesByVehicle" + getRidesByVehicle);

		// Rides already assigned to this vehicle (transfer_status IN (5,6,7,8), see
		// TransferRequestRepository.getVehicleFeed) pass through unchanged. Unassigned/pending
		// rides are additionally filtered to the same eligibility rules used when a ride is
		// first notified out (FireBaseMessagingService.notifyVehicles): matching vehicle type
		// (requested type plus the next larger interchangeable ones), the vehicle currently
		// available (not mid-job) and holding an FCM token — so a vehicle only sees a pending
		// ride in its feed if it would actually have been notified about it.
		boolean vehicleEligibleForNewRides = !vehicle.getOngoingStatus()
				&& vehicle.getFcmToken() != null
				&& !vehicle.getFcmToken().isEmpty();

		// Long-haul rides (over 100km) are only offered to vendor/fleet vehicles, not
		// individual (single-vehicle owner-operator) registrants. A missing vendor link or an
		// unset isIndividual (legacy vendors predating this flag) is treated as "not
		// individual", so existing vendors keep seeing long rides as before.
		boolean vehicleIsIndividual = vehicle.getTransferVendor() != null
				&& Boolean.TRUE.equals(vehicle.getTransferVendor().getIsIndividual());

		return getRidesByVehicle.stream()
				.filter(ride -> {
					if (ride.getVehicleId() != null) {
						return true; // already assigned to this vehicle — unchanged
					}

					VendorPickupVehicleEnum requestedType = ride.getVendorPickupVehicle();
					if (requestedType == null || !vehicleEligibleForNewRides) {
						return false;
					}

					if (vehicleIsIndividual && ride.getDistanceKm() != null && ride.getDistanceKm() > 100) {
						return false;
					}

					return VendorPickupVehicleEnum.getRequestedAndLarger(requestedType)
							.contains(vehicle.getVendorVehicle());
				})
				.collect(Collectors.toList());
	}

	@Override
	@Transactional
	public TransferRequestDetails requestTransferDelete(Long transferId) {
		TransferRequestDetails transfer = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
		CancelledRequestRepo.deleteByTransferRequest(transferId);
		walletTransactionRepo.deleteBydeleteByTransferRequest(transferId);
		transferRepo.delete(transfer);
		return transfer;
	}

	
	
	


}
