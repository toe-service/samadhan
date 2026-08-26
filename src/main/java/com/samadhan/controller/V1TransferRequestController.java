package com.samadhan.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.samadhan.entity.Ride;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VehicleTransfer;
import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.DimensionUnit;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.enums.rideStatusEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.security.TokenApi;
import com.samadhan.service.TransferRequestService;

@RestController
@RequestMapping("/transfer")
public class V1TransferRequestController {


@Autowired
TransferRequestService transferRequestService;

@Autowired
TokenApi tokenApi;


	  @PostMapping(value = "/requestRideTransfer")
	  public TransferRequestDetails requestRideTransfer(@RequestParam(required = false) ParcelTypeEnum parcelType,
	                                               @RequestParam(required = false) CarModelEnum carModel,
	                                               @RequestParam(required = false) String carNumber,
	                                               @RequestParam(required = false) BikeModelEnum bikeModel,
	                                               @RequestParam(required = false) String bikeNumber,
	                                               @RequestParam(required = false) Double packageWeight,
	                                               @RequestParam(required = false) String packageDescription,
	                                               @RequestParam(required = false) Long vendorId,
	                                               @RequestParam(required = false) String userType,
	                                               @RequestParam(required = false) Long userId,
	                                               @RequestParam(required = false) String userName,
	                                               @RequestParam(required = false) String userContact,
	                                               @RequestParam(required = false) String receiverName,
	                                               @RequestParam(required = false) String receiverContact,
	                                               @RequestParam double rideCost,
	                                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate pickupDate,
	                                               @RequestParam(required = false) String pickupSchedule,
	                                               @RequestParam String pickuplatitude,
	                                               @RequestParam String pickuplongitude,
	                                               @RequestParam String source,
	                                               @RequestParam String destination,
	                                               @RequestParam String destinationlatitude,
	                                               @RequestParam String destinationlongitude,
	                                               @RequestParam Double gstCost,
	                                               @RequestParam(required = false) Double rideWithoutTaxCalculation,
	                                               @RequestParam(required = false) Double loadingUnloading,
	                                               @RequestParam(required = false) Double packagingCost,
	                                               @RequestParam(required = false) Double length,
	                                               @RequestParam(required = false) Double width,
	                                               @RequestParam(required = false) Double height,
	                                               @RequestParam(required = false, defaultValue = "INCH") DimensionUnit dimensionUnit,
	                                               @RequestParam(required = false, defaultValue = "TRANSFERSERVICE") serviceTypeEnum serviceType,
	                                               @RequestParam(required = false) VendorPickupVehicleEnum vendorPickupVehicle,
	                                               @RequestParam(required = false, defaultValue = "false") Boolean helperRequired,
	                                               @RequestParam(required = false, defaultValue = "false") Boolean instantBooking,
	                                               @RequestParam(required = false, defaultValue = "0") Integer helperCount,
	                                               @RequestParam(required = false) VehicleCategoryEnum vehicleCategory,
	                                               @RequestParam(required = false) String homeType, @RequestParam(required = false) String packingType, 
	                                               @RequestParam(required = false) String goodsType,
		                                   	       @RequestParam(required = false, defaultValue = "0") Integer fromFloor,
		                                	       @RequestParam(required = false, defaultValue = "false") Boolean liftAvailable,
		                                	       @RequestParam(name = "distanceKm", required = false, defaultValue = "0") Double distanceInKm,
		                                	       @RequestParam(required = false, defaultValue = "true") Boolean isMovable) throws JsonProcessingException, FirebaseMessagingException {
	        if (serviceType == serviceTypeEnum.TRANSFERSERVICE) {

	            if (parcelType == null) {
	                throw new IllegalArgumentException("Parcel Type is required.");
	            }

	        }

	      
	        
	        
	        TransferRequestDetails rideTransfer = transferRequestService.requestRideTransfer(parcelType, carModel,
	                pickuplatitude, pickuplongitude, destinationlatitude, destinationlongitude,userId, rideCost, 
	                pickupDate, pickupSchedule,source, destination, carNumber, bikeModel, bikeNumber
	                , packageWeight, packageDescription, vendorId, userType, userName, userContact, gstCost, 
	                rideWithoutTaxCalculation, loadingUnloading, packagingCost, dimensionUnit, length, width, 
	                height, serviceType, vendorPickupVehicle, helperRequired, helperCount, vehicleCategory, 
	                instantBooking, homeType, packingType, goodsType, fromFloor, liftAvailable, receiverName, receiverContact, distanceInKm, isMovable);
	        return rideTransfer;
	  }
	  
	  // Customer-tracking endpoint. Requires a valid JWT (default security rule), and the token's
	  // own userId must match the path userId — otherwise any logged-in customer could browse
	  // another customer's ride history (names, phone numbers, addresses) just by changing the ID.
	  @GetMapping(value = "/rideTransferbyUser/{userId}")
	    public ResponseEntity<List<TransferRequestDetails>> getRidesTransferByuser(@PathVariable Long userId, HttpServletRequest httpRequest) {
			requireOwnUserId(userId, httpRequest);

			List<TransferRequestDetails> ridesByUser = transferRequestService.getTransferRidesByuser(userId);
		    if (ridesByUser.isEmpty()) {
		        return ResponseEntity.noContent().build(); // 204
		    }

			return ResponseEntity.ok(ridesByUser);
	    }

	  // Same ownership rule as above, checked against the ride's own linked user rather than a
	  // path userId.
	  @GetMapping(value = "/rideTransfer/{transferId}")
	    public ResponseEntity<TransferRequestDetails> getRidesByTransferId(@PathVariable Long transferId, HttpServletRequest httpRequest) {
			TransferRequestDetails ridesByTransferId = transferRequestService.getRidesByTransferId(transferId);

			Long rideUserId = ridesByTransferId.getUserDetails() != null ? ridesByTransferId.getUserDetails().getId() : null;
			requireOwnUserId(rideUserId, httpRequest);

			return ResponseEntity.ok(ridesByTransferId);
	    }

	  private void requireOwnUserId(Long ownerUserId, HttpServletRequest httpRequest) {
		  String authHeader = httpRequest.getHeader("Authorization");
		  String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
		  Long tokenUserId = jwt != null ? tokenApi.extractUserId(jwt) : null;

		  if (ownerUserId == null || tokenUserId == null || !tokenUserId.equals(ownerUserId)) {
			  throw new AccessDeniedException("You are not authorized to view this request");
		  }
	  }
	  
	  @GetMapping(value = "/showRidestoVendors/{transferId}")
	    public ResponseEntity<List<TransferRequestDetails>> showRidestoVendors(@PathVariable Long transferId) {
		  List<TransferRequestDetails> showRidestoVendors = transferRequestService.showRidestoVendors(transferId);		 
		  return ResponseEntity.ok(showRidestoVendors);
	    }
	  
//	  @PostMapping("/dispatchVehicleRequest/{requestId}")
//	  public ResponseEntity<String> dispatchVehicleRequest(@PathVariable Long requestId) {
//
//	      vehicleDispatchService.dispatchVehicleRequest(requestId);
//
//	      return ResponseEntity.ok("Vehicle notifications sent successfully.");
//	  }
	  
	  @GetMapping(value = "/rideTransferByDriver/{driverId}")
	    public ResponseEntity< List<TransferRequestDetails>> getRidesByDriverId(@PathVariable Long driverId) {
		  List<TransferRequestDetails> ridesByDriverId = transferRequestService.getRidesByDriverId(driverId);
		  if (ridesByDriverId.isEmpty()) {
		        return ResponseEntity.noContent().build(); // 204
		    }
		  
		  return ResponseEntity.ok(ridesByDriverId);
	    }
	  
	  @GetMapping(value = "/rideTransferByVehicle/{vehicleId}")
	    public ResponseEntity< List<TransferRequestDetails>> getrideTransferByVehicle(@PathVariable Long vehicleId) {
		  List<TransferRequestDetails> ridesByVehicle = transferRequestService.getrideTransferByVehicle(vehicleId);
		  if (ridesByVehicle.isEmpty()) {
		        return ResponseEntity.noContent().build(); // 204
		    }
		  
		  return ResponseEntity.ok(ridesByVehicle);
	    }
	  
	  @PostMapping(value = "/requestTransferApproval")
	  public TransferRequestDetails requestTransferApproval(@RequestParam Long transferId,
	                                               @RequestParam(required = false) Long vendorId,
	                                               @RequestParam int transferApproval
	                                               ,@RequestParam(required = false) String cancellationReason,@RequestParam(required = false) String userType
	                                               , @RequestParam(required = false, defaultValue = "TRANSFERSERVICE") serviceTypeEnum serviceType
	                                               , @RequestParam(required = false) Integer vehicleId
	                                               , @RequestParam(required = false, defaultValue = "Vehicle") String acceptedBy) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestTransferApproval( transferId, transferApproval,vendorId, cancellationReason, userType, serviceType, vehicleId, acceptedBy);
	        return rideTransfer;
	  }
	  
	  @PutMapping(value = "/requestTransferUpdate")
	  public TransferRequestDetails requestTransferUpdate(@RequestParam Long transferId,
			  							@RequestParam(required = false) Long driverId,@RequestParam(required = false) Integer vehicleId,
			  							@RequestParam(required = false) Integer rideStatusflag, @RequestParam(required = false) String userType) throws JsonProcessingException {
	        System.out.println("hi");
	        TransferRequestDetails rideTransfer = transferRequestService.requestTransferUpdate(transferId, driverId,vehicleId,rideStatusflag, userType);
	        return rideTransfer;
	  }
	  
	  @DeleteMapping("/requestTransferDelete/{transferId}")
	  public ResponseEntity<String> requestTransferDelete(@PathVariable Long transferId) {
	      transferRequestService.requestTransferDelete(transferId);
	      return ResponseEntity.ok("Deleted successfully");
	  }
	  
	  @GetMapping("/otpvalidate")
	  public ResponseEntity<Map<String, Object>> otpValidate(
	          @RequestParam Long transferId,
	          @RequestParam int otp,@RequestParam boolean flag,@RequestParam(required = false) String userType) {

	      boolean verified = transferRequestService.otpVerify(transferId, otp, flag, userType);

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
