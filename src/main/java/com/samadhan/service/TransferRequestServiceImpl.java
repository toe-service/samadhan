package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.UserRepository;



@Component
public class TransferRequestServiceImpl implements TransferRequestService{

	@Autowired
	TransferRequestRepository transferRepo;
	
	@Autowired
	UserRepository userRepo;
	
	@Autowired
	DriverRepository driverRepo;
	
	@Override
	public TransferRequestDetails requestRideTransfer(int vehicleType, int vehicleModel,  String pickuplatitude, String pickuplongitude,
			String destinationlatitude, String destinationlongitude, Long userId, double rideCost,LocalDate pickupDate, String pickupSchedule,String source, String destination) {
		
		Optional<UserDetails> userOpt=userRepo.findById(userId);
		UserDetails user=userOpt.get();
		
		TransferRequestDetails transferRequest=new TransferRequestDetails();
		transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleType]);
		transferRequest.setVehicleType(VehicleTypeEnum.values()[vehicleModel]);
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
	public TransferRequestDetails requestTransferApproval(Long userId, Long transferId, int transferApproval, Long driverId) {
		
		Optional<Driver> driveropt=driverRepo.findById(driverId);
		Driver driver=driveropt.get();
		
		Optional<TransferRequestDetails> transferdetailsopt=transferRepo.findById(transferId);
		TransferRequestDetails transferdetails=transferdetailsopt.get();
		
		transferdetails.setTransferStatus(rideStatusEnum.values()[transferApproval]);
		transferdetails.setDriver(driver);
		
		transferRepo.save(transferdetails);
		
		return transferdetails;
	}

	@Override
	public TransferRequestDetails getRidesByTransferId(Long transferId) {
		Optional<TransferRequestDetails> transferRidesByUserIdopt = transferRepo.findById(transferId);
		TransferRequestDetails transferRidesByUserId=transferRidesByUserIdopt.get();
		System.out.println("TransferRidesByUserId" + transferRidesByUserId);
		return transferRidesByUserId;
	}

	@Override
	public TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId) {
		Optional<Driver> driveropt=driverRepo.findById(driverId);
		Driver driver=driveropt.get();
		
		Optional<TransferRequestDetails> transferdetailsopt=transferRepo.findById(transferId);
		TransferRequestDetails transferdetails=transferdetailsopt.get();
		
		transferdetails.setTransferStatus(rideStatusEnum.READYFORPICKUP);
		transferdetails.setDriver(driver);
		
		transferRepo.save(transferdetails);
		
		return transferdetails;
		
	}

	@Override
	public boolean otpVerify(Long transferId, int otp) {
		Optional<TransferRequestDetails> transferdetailsopt=transferRepo.findById(transferId);
		TransferRequestDetails transferdetails=transferdetailsopt.get();
		
		if(transferdetails.getOtp()==otp) {
			transferdetails.setTransferStatus(rideStatusEnum.HANDOVER);
			return true;
		}
		
		
		return false;
	}
	
	
	

}
