package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.*;

@Repository
public interface ServiceCentreRepo extends JpaRepository<ServiceCentre, Long> {
	
	@Query(value="select * from Service_centre sc where sc.active=true" ,nativeQuery = true)
	ServiceCentre findByfilters();
	
	@Query(value ="SELECT s.*,lc.* FROM Service_centre s INNER JOIN location lc on s.location_id=lc.id WHERE " +
	           "(6371 * acos(cos(radians(:pickuplatitude)) * cos(radians(lc.latitude)) * cos(radians(lc.longitude) - radians(:pickuplongitude)) + " +
	           "sin(radians(:pickuplatitude)) * sin(radians(lc.latitude)))) <= :distance", nativeQuery = true)
	List<ServiceCentre> findAllServiceCentreByfilters(String pickuplatitude, String pickuplongitude,double distance);
}
