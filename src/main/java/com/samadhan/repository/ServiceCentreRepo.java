package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.*;

@Repository
public interface ServiceCentreRepo extends JpaRepository<ServiceCentre, Long> {
	
	@Query(value="select * from Service_centre sc where sc.city_name =:city and sc.active=true" ,nativeQuery = true)
	ServiceCentre findByfilters(String city);
}
