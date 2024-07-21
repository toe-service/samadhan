package com.samadhan.repository;

import java.util.List;

import com.samadhan.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<ServiceCentre, Long> {

	@Query(value ="SELECT d.* FROM driver d WHERE " +
	           "(6371 * acos(cos(radians(:pickuplatitude)) * cos(radians(d.driver_latitude)) * cos(radians(d.driver_longitude) - radians(:pickuplongitude)) + " +
	           "sin(radians(:pickuplatitude)) * sin(radians(d.driver_latitude)))) <= :distance", nativeQuery = true)
	List<Driver> findAllDriversByfilters(String pickuplatitude, String pickuplongitude,double distance);
	
}

