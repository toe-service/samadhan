package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VehicleTransfer;


public interface TransferRequestService{

	 public TransferRequestDetails requestRideTransfer(int vehicleType, int vehicleModel,  String pickuplatitude, String pickuplongitude,
			String destinationlatitude, String destinationlongitude, Long userId, double rideCost,LocalDate pickupDate, String pickupSchedule,String source, String destination);

	public List<TransferRequestDetails> getTransferRidesByuser(Long userId);

	public TransferRequestDetails requestTransferApproval(Long userId, Long transferId, int transferApproval);

	public TransferRequestDetails getRidesByTransferId(Long transferId);

	public TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId, Integer vehicleId);

	public boolean otpVerify(Long transferId, int otp);

	//TransferRequestDetails requestTransferUpdate(Long transferId, Long driverId);

}
