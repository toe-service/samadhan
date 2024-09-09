package com.samadhan.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.Ride;
import com.samadhan.entity.ServiceCentre;

@Repository
public interface RidesRepository  extends JpaRepository<Ride, Long>{
	
	@Query(value="select * from rides where user_id=:userId AND driver_id=:driverId AND id=:rideId" ,nativeQuery = true)
	Ride findByStatus(Long rideId, Long userId, Long driverId);

	@Query(value="select * from rides where user_id=:userId AND driver_id=:driverId AND ride_response_time > NOW() - INTERVAL 15 MINUTE" ,nativeQuery = true)
	Ride existRide(Long driverId, long userId);

//	@Query(value="select * from ride" ,nativeQuery = true)
//	Ride findByStatus();
//	
//	@Query(value="select * from ride where user_id=:id" ,nativeQuery = true)
//	List<Ride> findByUserId(Long userId);

}
