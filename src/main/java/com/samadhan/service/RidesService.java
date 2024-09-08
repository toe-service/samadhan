package com.samadhan.service;


import java.util.List;
import com.samadhan.entity.Ride;

public interface RidesService {

	Ride popupRemove(Long rideId,Long userId,Long driverId);

	List<Ride> getRidesByuser(Long userId);
	
}
