package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.Ride;
import com.samadhan.entity.ServiceCentre;

@Repository
public interface RidesRepository  extends JpaRepository<ServiceCentre, Long>{

	@Query(value="select * from ride" ,nativeQuery = true)
	Ride findByStatus();

}
