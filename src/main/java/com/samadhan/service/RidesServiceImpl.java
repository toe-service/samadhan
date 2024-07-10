package com.samadhan.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Ride;
import com.samadhan.repository.RidesRepository;

@Service
public class RidesServiceImpl implements RidesService{

	@Autowired
	RidesRepository rideRepo;
	
	@Override
	public Ride popupRemove(Ride ride) {
		
		long userId=0l;
		long rideId=ride.getId();
		long driverId=0l;
		
		Ride rides=rideRepo.findByStatus();
		
		return rides;
	}

}
