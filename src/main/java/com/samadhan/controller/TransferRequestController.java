package com.samadhan.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.service.TransferRequestService;

@RestController
@RequestMapping("/transfer")
public class TransferRequestController {
	

@Autowired
TransferRequestService transferRequestService;
	
	
	  @PostMapping(value = "/requestRideTransfer")
	  public TransferRequestDetails requestRideTransfer(@RequestParam int vehicleType,
	                                               @RequestParam String VehicleModel,
	                                               @RequestParam Long userId,
	                                              @RequestParam int rideCost,
	                                               @RequestParam String city,
	                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
	                                               @RequestParam String pickupSchedule,
	                                               @RequestParam String pickuplatitude,
	                                               @RequestParam String pickuplongitude,
	                                               @RequestParam String destinationlatitude,
	                                               @RequestParam String destinationlongitude) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestRideTransfer(vehicleType, VehicleModel, city,
	                pickuplatitude, pickuplongitude, destinationlatitude, destinationlongitude,userId, rideCost, pickupDate, pickupSchedule);
	        return rideTransfer;
	  }
	  
	  @GetMapping(value = "/rideTransferbyUser/{userId}")
	    public ResponseEntity<List<TransferRequestDetails>> getRidesTransferByuser(@PathVariable Long userId) {
			List<TransferRequestDetails> ridesByUser = transferRequestService.getTransferRidesByuser(userId);
			return ResponseEntity.ok(ridesByUser);
	    }
	  
	  @GetMapping(value = "/rideTransfer/{transferId}")
	    public ResponseEntity<TransferRequestDetails> getRidesByTransferId(@PathVariable Long transferId) {
			TransferRequestDetails ridesByTransferId = transferRequestService.getRidesByTransferId(transferId);
			return ResponseEntity.ok(ridesByTransferId);
	    }
	  
	  @PostMapping(value = "/requestTransferApproval")
	  public TransferRequestDetails requestTransferApproval(@RequestParam Long userId,
	                                               @RequestParam Long transferId,
	                                               @RequestParam Long driverId,
	                                               @RequestParam int transferApproval) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestTransferApproval(userId, transferId, transferApproval, driverId);
	        return rideTransfer;
	  }

}
