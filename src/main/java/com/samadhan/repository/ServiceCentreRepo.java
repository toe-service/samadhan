package com.samadhan.repository;

import java.util.List;

import com.samadhan.dto.ServiceCentreWrapper;
import com.samadhan.enums.serviceTypeEnum;
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
	           "sin(radians(:pickuplatitude)) * sin(radians(lc.latitude)))) <= :distance AND s.service_type=:serviceType", nativeQuery = true)
	List<ServiceCentre> findAllServiceCentreByfilters(String pickuplatitude, String pickuplongitude, double distance, Integer serviceType);

	@Query(value ="SELECT d.driver_latitude,d.driver_longitude,d.driver_contact_number,d.driver_name,d.service_centre_id,d.driver_token FROM driver d  WHERE " +
			"(6371 * acos(cos(radians(:pickuplatitude)) * cos(radians(d.driver_latitude)) * cos(radians(d.driver_longitude) - radians(:pickuplongitude)) + " +
			"sin(radians(:pickuplatitude)) * sin(radians(d.driver_latitude)))) <= :distance", nativeQuery = true)
    List<Object> findAllServiceCentreDriversByfilters(String pickuplatitude, String pickuplongitude, double distance);
}
