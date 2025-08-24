package com.samadhan.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.samadhan.entity.Ride;
import com.samadhan.repository.RidesRepository;

@Service
public class RidesServiceImpl implements RidesService{

	@Autowired
	RidesRepository rideRepo;
	
	@Override
	public Ride popupRemove(Long rideId,Long userId,Long driverId) {
		
		Ride rides=rideRepo.findByStatus(rideId,userId,driverId);
		
		return rides;
	}

	@Override
	public List<Ride> getRidesByuser(Long userId) {
		

		List<Ride> ridesByUserId=rideRepo.findByUserId(userId);
		System.out.println("ridesByUserId"+ridesByUserId);
		return ridesByUserId;
		
//		return null;
	}

}
