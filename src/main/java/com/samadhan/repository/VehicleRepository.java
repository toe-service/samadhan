package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.Vehicle;

public interface VehicleRepository   extends JpaRepository<Vehicle, Long> {

	@Query(value="select * from vehicle where transfer_id=:vendorId",nativeQuery=true)
	List<Vehicle> findByVendorId(Long vendorId);

}
