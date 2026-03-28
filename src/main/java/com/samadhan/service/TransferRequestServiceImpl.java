package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import com.samadhan.enums.VehicleTypeEnum;
import com.samadhan.enums.rideStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
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
	public TransferRequestDetails requestTransferApproval(Long transferId, int transferApproval, Long vendorId) {
		

		LocalDateTime dateTime=LocalDateTime.now();
		
		Optional<TransferRequestDetails> transferdetailsopt=transferRepo.findById(transferId);
		TransferRequestDetails transferdetails=transferdetailsopt.get();
		
		Optional<TransferVendor> transferVendoropt=transferVendorRepo.findById(vendorId);
		TransferVendor transferVendor=transferVendoropt.get();
		
		
		
		transferdetails.setTransferStatus(rideStatusEnum.values()[transferApproval]);
		transferdetails.setRequestApprovalDate(dateTime);
		transferdetails.settransferVendor(transferVendor);
		
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
	public TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId, Integer vehicleId, Integer rideStatus) {

		Optional<TransferRequestDetails> transferdetailsopt=transferRepo.findById(transferId);
		TransferRequestDetails transferdetails=transferdetailsopt.get();

		if (driverId != null) {
		LocalDateTime dateTime=LocalDateTime.now();
		Optional<Driver> driveropt=driverRepo.findById(driverId);
		Driver driver=driveropt.get();
		transferdetails.setDriver(driver);
		transferdetails.setDriverAssignDateTime(dateTime);
		transferdetails.setTransferStatus(rideStatusEnum.READYFORPICKUP);
		}


		if (vehicleId != null && vehicleId != 0) {
			LocalDateTime dateTime=LocalDateTime.now();
			transferdetails.setVehicleAssignDateTime(dateTime);
			transferdetails.setVehicleId(vehicleId);
		//	transferdetails.setTransferStatus(rideStatusEnum.ONGOING);
		}
		
		if(rideStatus != null && rideStatus==0) {
			LocalDateTime dateTime=LocalDateTime.now();
			transferdetails.setRidestartTime(dateTime);
			transferdetails.setTransferStatus(rideStatusEnum.ONGOING);
		}else if(rideStatus != null && rideStatus==1) {
			LocalDateTime dateTime=LocalDateTime.now();
			transferdetails.setRideendTime(dateTime);
			transferdetails.setTransferStatus(rideStatusEnum.COMPLETED);
		}

		transferRepo.save(transferdetails);

		return transferdetails;

	}

	@Override
	public boolean otpVerify(Long transferId, int otp, boolean flag) {
		Optional<TransferRequestDetails> transferdetailsopt=transferRepo.findById(transferId);
		TransferRequestDetails transferdetails=transferdetailsopt.get();

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
