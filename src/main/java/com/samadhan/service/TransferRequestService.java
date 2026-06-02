package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.ParcelTypeEnum;


public interface TransferRequestService{

//	 public TransferRequestDetails requestRideTransfer(int vehicleType, int vehicleModel,  String pickuplatitude, String pickuplongitude,
//			String destinationlatitude, String destinationlongitude, Long userId, double rideCost,LocalDate pickupDate, String pickupSchedule,String source, String destination);

	public List<TransferRequestDetails> getTransferRidesByuser(Long userId);

	public TransferRequestDetails requestTransferApproval(Long transferId, int transferApproval, Long vendorId, String cancellationReason);

	public TransferRequestDetails getRidesByTransferId(Long transferId);

	public TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId, Integer vehicleId, Integer rideStatusflag);

	public boolean otpVerify(Long transferId, int otp, boolean flag);

	public List<TransferRequestDetails> getRidesByDriverId(Long driverId);

	public List<TransferRequestDetails> showRidestoVendors(Long transferId);

	public List<TransferRequestDetails> getrideTransferByVehicle(Long vehicleId);

	public TransferRequestDetails requestTransferDelete(Long transferId);

	public TransferRequestDetails requestRideTransfer(ParcelTypeEnum parcelType, CarModelEnum carModel,
			String pickuplatitude, String pickuplongitude, String destinationlatitude, String destinationlongitude,
			Long userId, double rideCost, LocalDate pickupDate, String pickupSchedule, String source,
			String destination, String carNumber, BikeModelEnum bikeModel, String bikeNumber, Double packageWeight,
			String packageDescription, Long vendorId, String userType);

}
