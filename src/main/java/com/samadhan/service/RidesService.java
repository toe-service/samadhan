package com.samadhan.service;


import java.util.List;
import com.samadhan.entity.Ride;
import com.samadhan.exception.SamadhanException;

public interface RidesService {

	Ride popupRemove(Long rideId,Long userId,Long driverId);

	List<Ride> getRidesByuser(Long userId);

	Ride rideStartEnd(Long userId, Boolean rideFlag, Long driverId, int otp, Long rideId) throws SamadhanException;

	int generateOtp();

	String generateRideId(Long userId);
}
