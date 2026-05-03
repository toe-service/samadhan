package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.Vehicle;

public interface VehicleRepository   extends JpaRepository<Vehicle, Long> {

	@Query(value="select * from vehicle where transfer_id=:vendorId",nativeQuery=true)
	List<Vehicle> findByVendorId(Long vendorId);

	@Query(value="select * from vehicle where transfer_id=:vendorId and ongoing_status=:isActive",nativeQuery=true)
	List<Vehicle> findByActiveVendorId(Long vendorId, boolean isActive);

	@Query(value="select * from vehicle where user_name=:userName and password=:password",nativeQuery=true)
	Vehicle findByUserNamePassword(String userName, String password);

}
