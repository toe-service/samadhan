package com.samadhan.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.service.TransferRequestService;

@RestController
@RequestMapping("/transfer")
public class V1TransferRequestController {
	

@Autowired
TransferRequestService transferRequestService;
	
	
	  @PostMapping(value = "/requestRideTransfer")
	  public TransferRequestDetails requestRideTransfer(@RequestParam int vehicleType,
	                                               @RequestParam int VehicleModel,
	                                               @RequestParam Long userId,
	                                              @RequestParam double rideCost,
	                                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
	                                               @RequestParam String pickupSchedule,
	                                               @RequestParam String pickuplatitude,
	                                               @RequestParam String pickuplongitude,
	                                               @RequestParam String source,
	                                               @RequestParam String destination,
	                                               @RequestParam String destinationlatitude,
	                                               @RequestParam String destinationlongitude) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestRideTransfer(vehicleType, VehicleModel,
	                pickuplatitude, pickuplongitude, destinationlatitude, destinationlongitude,userId, rideCost, pickupDate, pickupSchedule,source, destination);
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
	  
	  @GetMapping(value = "/rideTransferByDriver/{driverId}")
	    public ResponseEntity< List<TransferRequestDetails>> getRidesByDriverId(@PathVariable Long driverId) {
		  List<TransferRequestDetails> ridesByDriverId = transferRequestService.getRidesByDriverId(driverId);
			return ResponseEntity.ok(ridesByDriverId);
	    }
	  
	  @PostMapping(value = "/requestTransferApproval")
	  public TransferRequestDetails requestTransferApproval(@RequestParam Long userId,
	                                               @RequestParam Long transferId,
	                                               @RequestParam int transferApproval) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestTransferApproval(userId, transferId, transferApproval);
	        return rideTransfer;
	  }
	  
	  @PutMapping(value = "/requestTransferUpdate")
	  public TransferRequestDetails requestTransferUpdate(@RequestParam Long transferId,
			  							@RequestParam(required = false) Long driverId,@RequestParam(required = false) Integer vehicleId,
			  							@RequestParam(required = false) Integer rideStatusflag) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestTransferUpdate(transferId, driverId,vehicleId,rideStatusflag);
	        return rideTransfer;
	  }
	  
	  @GetMapping("/otpvalidate")
	  public ResponseEntity<Map<String, Object>> otpValidate(
	          @RequestParam Long transferId,
	          @RequestParam int otp,@RequestParam boolean flag) {

	      boolean verified = transferRequestService.otpVerify(transferId, otp, flag);

	      Map<String, Object> response = new HashMap<>();
	      response.put("success", verified); // always boolean
			if (flag) {
			response.put("transferStatus", rideStatusEnum.COMPLETED);
			} else {
				response.put("transferStatus", rideStatusEnum.HANDOVER);
			}
	     

	      return ResponseEntity.ok(response);
	  }

}
