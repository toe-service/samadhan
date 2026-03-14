package com.samadhan.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VehicleTransfer;


public interface TransferRequestService{

	 public TransferRequestDetails requestRideTransfer(int vehicleType, String vehicleModel, String city, String pickuplatitude, String pickuplongitude,
			String destinationlatitude, String destinationlongitude, Long userId, int rideCost,LocalDate pickupDate, String pickupSchedule);

	public List<TransferRequestDetails> getTransferRidesByuser(Long userId);

	public TransferRequestDetails requestTransferApproval(Long userId, Long transferId, int transferApproval, Long driverId);

	public TransferRequestDetails getRidesByTransferId(Long transferId);

}
