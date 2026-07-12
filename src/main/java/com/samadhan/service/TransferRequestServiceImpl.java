package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.exception.ResourceNotFoundException;
import com.samadhan.exception.SubscriptionSuspendedException;
import com.samadhan.exception.WalletLowBalanceException;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Objects;
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
				Double loadingUnloading, Double packagingCost, DimensionUnit dimensionUnit, Double length, Double width, Double heigth) {	
		
		
		TransferVendor vendor = null;
		
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

		ParcelDetails parcelDetails=new ParcelDetails();
		parcelDetails.setParcelType(parcelType);
		
		if(parcelType.getType().equalsIgnoreCase("Car")) {
			
		packageWeight = carModel.getAverageWeightKg();
		parcelDetails.setCarModel(carModel);
		parcelDetails.setParcelWeight(packageWeight);
		parcelDetails.setCarNumber(carNumber);
		}
		
		if(parcelType.getType().equalsIgnoreCase("Bike")) {
		packageWeight = bikeModel.getAverageWeightKg();
		parcelDetails.setBikeModel(bikeModel);
		parcelDetails.setParcelWeight(packageWeight);
		parcelDetails.setBikeNumber(bikeNumber);
		}
		
		if(parcelType.getType().equalsIgnoreCase("Package")) {
		parcelDetails.setParcelWeight(packageWeight);
		parcelDetails.setPackageDescription(packageDescription);
		parcelDetails.setLength(length);
		parcelDetails.setWidth(width);
		parcelDetails.setHeight(heigth);
		parcelDetails.setDimensionUnit(dimensionUnit);
		}
		
		TransferRequestDetails transferRequest=new TransferRequestDetails();
//		transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleType]);
	//	transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleModel]);
		transferRequest.setParcelDetails(parcelDetails);
		LocalDate currentDate=LocalDate.now();
		LocalTime cuurentTime=LocalTime.now();
		LocalDateTime currentDateTime=LocalDateTime.now();
		transferRequest.setPickupDate(pickupDate);
		transferRequest.setPickupSchedule(pickupSchedule);
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
		transferRequest.setPackagingCost(packagingCost);
		//transferRequest.setDimensionUnit(dimensionUnit);
		
		//transferRequest.setTransferCalculation(rideCost);
		if(userType!=null && userType.equalsIgnoreCase("Vendor")){
//			TransferVendor vendor = null;
			Optional<UserDetails> userDetailopt =
			        userRepo.findByUserContactNumber(userContact);
			UserDetails userDetail =userDetailopt.get(); 
			       

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
		}else {
			transferRequest.setUserType(userType);
		transferRequest.setTransferStatus(rideStatusEnum.PENDING);
		}
		
		transferRepo.save(transferRequest);
		
		
		return transferRequest;
	}

	@Override
	public List<TransferRequestDetails> getTransferRidesByuser(Long userId) {
		
		List<TransferRequestDetails> transferRidesByUserId = transferRepo.findTransferRideByUserId(userId);
		System.out.println("TransferRidesByUserId" + transferRidesByUserId);
		
		
		return transferRidesByUserId;
		
	}

	@Override
	public TransferRequestDetails requestTransferApproval(Long transferId, int transferApproval, Long vendorId, String cancellationReason,String userType) {

		LocalDateTime dateTime = LocalDateTime.now();
		


		TransferRequestDetails transferdetails = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
		
		TransferVendor existingVendor=transferdetails.getTransferVendor();
		
		if (existingVendor != null) {
		    throw new RuntimeException("This request is already accepted.");
		}

		TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));
	
		if(transferApproval==1 && (userType !=null && userType.equalsIgnoreCase("User"))) {
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
			transferRepo.save(transferdetails);
			return transferdetails;
		}
		else {

		transferdetails.setTransferStatus(rideStatusEnum.values()[transferApproval]);
		transferdetails.setRequestApprovalDate(dateTime);
		transferdetails.setTransferVendor(transferVendor);

		transferRepo.save(transferdetails);

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

		if (driverId != null) {
			Driver driver = driverRepo.findById(driverId)
					.orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

			transfer.setDriver(driver);
			transfer.setDriverAssignDateTime(dateTime);
			transfer.setOtp(otp);
			transfer.setTransferStatus(rideStatusEnum.READYFORPICKUP);
			transferRepo.save(transfer);
		}

		// 🔹 Vehicle Assignment
		if (vehicleId != null && vehicleId != 0) {
//			long vehiId =(long) transfer.getVehicleId();
//			Optional<Vehicle> vehicleopt=vehicleRepo.findById(vehicleId);
			if (userType != null && userType.equalsIgnoreCase("User")) {
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
			transfer.setTransferStatus(rideStatusEnum.VEHICLEASSIGNED);
			transferRepo.save(transfer);
		}
 
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
			
			if(userType!=null && userType.equalsIgnoreCase("User")) {
			
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
			transferdetails.setTransferStatus(rideStatusEnum.COMPLETED);
			transferRepo.save(transferdetails);
			
			if(userType!=null && userType.equalsIgnoreCase("User")) {
			
			long vendorId=transferdetails.getTransferVendor().getId();
			
			VendorWallet wallet = walletRepository.findByVendor(vendorId);
			
			TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
					.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));
			
			double acceptanceFee = calculateCompletioneFee(transferdetails);
			
			if(wallet.getBalance() < -100){
			    throw new RuntimeException(
			        "Insufficient wallet balance. Please recharge."
			    );
			}

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
	 	 
	 	
	    
		System.out.println("showRidestoVendors" + showRidestoVendors);
		
		return showRidestoVendors;
	}

	@Override
	public List<TransferRequestDetails> getrideTransferByVehicle(Long vehicleId) {
		List<TransferRequestDetails> getRidesByVehicle = transferRepo.getRidesByVehicle(vehicleId);
		System.out.println("getRidesByVehicle" + getRidesByVehicle);
		
		return getRidesByVehicle;
	}

	@Override
	public TransferRequestDetails requestTransferDelete(Long transferId) {
		TransferRequestDetails transfer = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
		CancelledRequestRepo.deleteByTransferRequest(transferId);
		transferRepo.delete(transfer);
		return transfer;
	}

	
	
	


}
