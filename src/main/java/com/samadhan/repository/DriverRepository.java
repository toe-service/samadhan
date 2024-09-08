package com.samadhan.repository;

import java.util.List;

import com.samadhan.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Component
public interface DriverRepository extends JpaRepository<Driver, Long> {

//	@Query(value ="SELECT d.*,sc.* FROM driver d INNER JOIN Service_centre sc on d.service_centre_id=sc.id WHERE " +
//	           "(6371 * acos(cos(radians(:pickuplatitude)) * cos(radians(d.driver_latitude)) * cos(radians(d.driver_longitude) - radians(:pickuplongitude)) + " +
//	           "sin(radians(:pickuplatitude)) * sin(radians(d.driver_latitude)))) <= :distance", nativeQuery = true)
	
	@Query("SELECT d FROM Driver d WHERE " +
	           "(6371 * acos(cos(radians(:pickuplatitude)) * cos(radians(d.driverLatitude)) * cos(radians(d.driverLongitude) - radians(:pickuplongitude)) + " +
	           "sin(radians(:pickuplatitude)) * sin(radians(d.driverLatitude)))) <= :distance")
	List<Driver> findAllDriversByfilters(String pickuplatitude, String pickuplongitude,double distance);
	
}

