package com.samadhan.controller;

import java.util.List;
import java.util.Map;
import com.samadhan.response.Error;
import com.samadhan.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Ride;
import com.samadhan.exception.SamadhanException;
import com.samadhan.service.RidesService;

@RestController
@RequestMapping(value = "/ride")
public class RidesController {
	
	@Autowired
	RidesService ridesService;
	
	@GetMapping(value = "/pop-remove")
    public ResponseEntity<Ride> popupRemove(@RequestParam Long rideId,@RequestParam Long userId,@RequestParam Long driverId) {
		Ride response = ridesService.popupRemove(rideId,userId,driverId);
		return ResponseEntity.ok(response);
    }
	
	
	@GetMapping(value = "/ride/{userId}")
    public ResponseEntity<List<Ride>> getRidesByuser(@PathVariable Long userId) {
		List<Ride> ridesByUser = ridesService.getRidesByuser(userId);
		return ResponseEntity.ok(ridesByUser);
    }

	@GetMapping(value = "/otp")
	public ResponseEntity<Object> generateOtp() {
	try{
		int otp = ridesService.generateOtp();
		//return ResponseEntity.ok(otp);
		return ResponseEntity
				.ok(ResponseUtil.populateResponseObject(otp, "SUCCESS", null));
	}catch(Exception e){
		throw e;



	}

	}

	@GetMapping(value = "/rideStartEnd")
	public ResponseEntity<Object> rideStartEnd(@RequestParam Long userId, @RequestParam Long driverId,@RequestParam int otp, @RequestParam Boolean rideFlag, @RequestParam Long rideId) throws SamadhanException {
		try {
			Ride ride = ridesService.rideStartEnd(userId, rideFlag, driverId, otp, rideId);

			return ResponseEntity
					.ok(ResponseUtil.populateResponseObject(ride, "SUCCESS", null));
		}catch(SamadhanException e){
			throw e;



		}

      }



	@GetMapping(value = "/generateRideId")
	public ResponseEntity<Object> generateRideId(@RequestParam Long userId)  throws SamadhanException{
		try{
		String rideId = ridesService.generateRideId(userId);

		return ResponseEntity
				.ok(ResponseUtil.populateResponseObject(rideId, "SUCCESS", null));
		}catch(Exception e){
			throw e;

		}
	}
	

}
