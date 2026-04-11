package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.transaction.Transactional;

import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.exception.ResourceNotFoundException;

import org.hibernate.annotations.common.util.impl.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.api.client.util.Objects;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.UserRepository;



@Component
public class TransferRequestServiceImpl implements TransferRequestService{

	@Autowired
	TransferRequestRepository transferRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	DriverRepository driverRepo;
	
	@Autowired
	TransferVendorRepository transferVendorRepo;
	
	//private static final Logger logger = LoggerFactory.logger(TransferRequestService.class);
	
	@Override
	public TransferRequestDetails requestRideTransfer(int vehicleType, int vehicleModel,  String pickuplatitude, String pickuplongitude,
			String destinationlatitude, String destinationlongitude, Long userId, double rideCost,LocalDate pickupDate, String pickupSchedule,String source, String destination) {
		
		 UserDetails user = userRepo.findById(userId)
		            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		
		TransferRequestDetails transferRequest=new TransferRequestDetails();
		transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleType]);
	//	transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleModel]);
		LocalDate currentDate=LocalDate.now();
		LocalTime cuurentTime=LocalTime.now();
		transferRequest.setPickupDate(pickupDate);
		transferRequest.setPickupSchedule(pickupSchedule);
		transferRequest.setRideCost(rideCost);
		transferRequest.setUserDetails(user);
		transferRequest.setDestinationLatitude(destinationlatitude);
		transferRequest.setDestinationLongitude(destinationlongitude);
		transferRequest.setSourceLatitude(pickuplatitude);
		transferRequest.setSourceLongitude(pickuplongitude);
		transferRequest.setSource(source);
		transferRequest.setDestination(destination);
		//transferRequest.setTransferCalculation(rideCost);
		transferRequest.setTransferStatus(rideStatusEnum.PENDING);
		
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
	public TransferRequestDetails requestTransferApproval(Long transferId, int transferApproval, Long vendorId) {

		LocalDateTime dateTime = LocalDateTime.now();

		TransferRequestDetails transferdetails = transferRepo.findById(transferId)
				.orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));

		TransferVendor transferVendor = transferVendorRepo.findById(vendorId)
				.orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));

		transferdetails.setTransferStatus(rideStatusEnum.values()[transferApproval]);
		transferdetails.setRequestApprovalDate(dateTime);
		transferdetails.settransferVendor(transferVendor);

		transferRepo.save(transferdetails);

		return transferdetails;
	}

//	@Override
//	public TransferRequestDetails getRidesByTransferId(Long transferId) {
//		Optional<TransferRequestDetails> transferRidesByUserIdopt = transferRepo.findById(transferId);
//		TransferRequestDetails transferRidesByUserId=transferRidesByUserIdopt.get();
//		System.out.println("TransferRidesByUserId" + transferRidesByUserId);
//		return transferRidesByUserId;
//	}
	
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

		if (driverId != null) {
			Driver driver = driverRepo.findById(driverId)
					.orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

			transfer.setDriver(driver);
			transfer.setDriverAssignDateTime(dateTime);
			transfer.setTransferStatus(rideStatusEnum.READYFORPICKUP);
		}

		// 🔹 Vehicle Assignment
		if (vehicleId != null && vehicleId != 0) {
			transfer.setVehicleId(vehicleId);
			transfer.setVehicleAssignDateTime(dateTime);
			transfer.setTransferStatus(rideStatusEnum.VEHICLEASSIGNED);
		}

		if (rideStatus != null && rideStatus == 0) {

			transfer.setRidestartTime(dateTime);
			transfer.setTransferStatus(rideStatusEnum.ONGOING);
		} else if (rideStatus != null && rideStatus == 1) {

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
		  
		 
		if(transferdetails.getOtp()==otp) {
			LocalDateTime dateTime=LocalDateTime.now();
			transferdetails.setHandoveredDateTime(dateTime);

			if(flag) {
			transferdetails.setTransferStatus(rideStatusEnum.COMPLETED);	
			}else {
			transferdetails.setTransferStatus(rideStatusEnum.HANDOVER);
			}
			transferRepo.save(transferdetails);
			return true;
		}

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
		System.out.println("showRidestoVendors" + showRidestoVendors);
		
		return showRidestoVendors;
	}

	@Override
	public List<TransferRequestDetails> getrideTransferByVehicle(Long vehicleId) {
		List<TransferRequestDetails> getRidesByVehicle = transferRepo.getRidesByVehicle(vehicleId);
		System.out.println("getRidesByVehicle" + getRidesByVehicle);
		
		return getRidesByVehicle;
	}

	
	
	


}
