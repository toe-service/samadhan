package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.security.SecureRandom;

import javax.transaction.Transactional;

import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.exception.ResourceNotFoundException;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Objects;
import com.samadhan.entity.CancelledRequest;
import com.samadhan.entity.Driver;
import com.samadhan.entity.ParcelDetails;
import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.entity.VendorWallet;
import com.samadhan.entity.WalletTransaction;
import com.samadhan.repository.CancelledRequestRepository;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.UserRepository;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.repository.VendorWalletRepository;
import com.samadhan.repository.WalletTransactionRepo;
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
	
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	
	//private static final Logger logger = LoggerFactory.logger(TransferRequestService.class);
	
	@Override
//	public TransferRequestDetails requestRideTransfer(int vehicleType, int vehicleModel,  String pickuplatitude, String pickuplongitude,
//			String destinationlatitude, String destinationlongitude, Long userId, double rideCost,LocalDate pickupDate, String pickupSchedule,String source, String destination) {
	
		public TransferRequestDetails requestRideTransfer(ParcelTypeEnum parcelType, CarModelEnum carModel,
				String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude,
				Long userId, double rideCost, LocalDate pickupDate, String pickupSchedule, String source,
				String destination, String carNumber, BikeModelEnum bikeModel, String bikeNumber, Double packageWeight,
				String packageDescription, Long vendorId, String userType, String userName, String userContact) {	
		
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
		//transferRequest.setTransferCalculation(rideCost);
		if(userType!=null && userType.equalsIgnoreCase("Vendor")){
			TransferVendor vendor = null;
			UserDetails userDetail=new UserDetails();
			
			userDetail.setUserName(userName);
			userDetail.setUserContactNumber(userContact);
			

			if(vendorId != null){
			    vendor = transferVendorRepo.findById(vendorId)
			            .orElseThrow(() -> new ResourceNotFoundException(
			                    "Vendor not found with id: " + vendorId));
			}

			transferRequest.setTransferVendor(vendor);
			transferRequest.setTransferStatus(rideStatusEnum.ACCEPTED);
			transferRequest.setRequestApprovalDate(currentDateTime);
			transferRequest.setUserType(userType);
		}else {
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
	public TransferRequestDetails requestTransferApproval(Long transferId, int transferApproval, Long vendorId, String cancellationReason) {

		LocalDateTime dateTime = LocalDateTime.now();
		


		TransferRequestDetails transferdetails = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
		
		TransferVendor existingVendor=transferdetails.getTransferVendor();
		
		if (existingVendor != null) {
		    throw new RuntimeException("This request is already accepted.");
		}

		TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));
	
		if(transferApproval==1) {
		VendorWallet wallet = walletRepository.findByVendor(vendorId);
		
		double acceptanceFee = calculateAcceptanceFee(transferdetails);
		
		if(wallet.getBalance() < acceptanceFee){
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
		}else {

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
		
		return Math.round(rideCost * 0.025 * 100.0) / 100.0;
	}

	@Override
	public TransferRequestDetails getRidesByTransferId(Long transferId) {

		return transferRepo.findById(transferId)
		        .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));
	}

	@Override
	@Transactional
	public TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId, Integer vehicleId,
			Integer rideStatus) {

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
		}

		// 🔹 Vehicle Assignment
		if (vehicleId != null && vehicleId != 0) {
//			long vehiId =(long) transfer.getVehicleId();
//			Optional<Vehicle> vehicleopt=vehicleRepo.findById(vehicleId);
			 Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			transfer.setVehicleId(vehicle);
			transfer.setVehicleAssignDateTime(dateTime);
			transfer.setTransferStatus(rideStatusEnum.VEHICLEASSIGNED);
		}

		if (rideStatus != null && rideStatus == 0) {
			//long vehiId = (long) vehicleId;
			 Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			
			vehicle.setOngoingStatus(true);
			vehicleRepo.save(vehicle);
			
			transfer.setRidestartTime(dateTime);
			transfer.setClosureotp(otp);
			
			transfer.setTransferStatus(rideStatusEnum.ONGOING);
		} else if (rideStatus != null && rideStatus == 1) {
			Vehicle vehicle = vehicleRepo.findById(Long.valueOf(vehicleId))
		                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
			
			vehicle.setOngoingStatus(true);
			vehicleRepo.save(vehicle);
			transfer.setRideendTime(dateTime);
			transfer.setTransferStatus(rideStatusEnum.COMPLETED);
		}

		transferRepo.save(transfer);

		return transfer;

	}

	@Override
	public boolean otpVerify(Long transferId, int otp, boolean flag) {

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
		
		VendorWallet wallet=walletRepository.findByVendor(transferId);
		
	 	if (wallet == null) {
	        throw new RuntimeException("Wallet not found");
	    }

	    // 3. Define lead cost
	    double leadCost = 20; // ₹10 per lead (example)

	    // 4. Check balance
	    if (wallet.getBalance() < leadCost) {
	        throw new RuntimeException("Insufficient wallet balance");
	    }
		Optional<TransferVendor> vendor=transferVendorRepo.findById(transferId);
	    
	    wallet.setBalance(wallet.getBalance() - leadCost);
        walletRepository.save(wallet);

        // Optional: log transaction
        WalletTransaction transaction = new WalletTransaction();
        transaction.setVendor(vendor.get());
        transaction.setAmount(20.0);
        transaction.setTransactionType("DEBIT");
        transaction.setDescription("LEAD_VIEW");
        walletTransactionRepo.save(transaction);
		
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
