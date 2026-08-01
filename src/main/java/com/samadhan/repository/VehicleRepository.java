package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.samadhan.entity.Vehicle;

public interface VehicleRepository   extends JpaRepository<Vehicle, Long> {

	@Query(value="select * from vehicle where transfer_id=:vendorId",nativeQuery=true)
	List<Vehicle> findByVendorId(Long vendorId);

	@Query(value="select * from vehicle where transfer_id=:vendorId and ongoing_status=:isActive",nativeQuery=true)
	List<Vehicle> findByActiveVendorId(Long vendorId, boolean isActive);

	@Query(value="select * from vehicle where user_name=:userName and password=:password",nativeQuery=true)
	Vehicle findByUserNamePassword(String userName, String password);
	
	@Query(value =
	        "SELECT * " +
	        "FROM vehicle v " +
	        "WHERE v.fcm_token IS NOT NULL " +
	        "AND v.ongoing_status = false " +
	        "AND ( :vehicleType IS NULL OR v.vendor_vehicle_type = :vehicleType ) " +
	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(v.vehicle_longitude) AS DECIMAL(12,8)), " +
	        "      CAST(TRIM(v.vehicle_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(:pickupLongitude AS DECIMAL(12,8)), " +
	        "      CAST(:pickupLatitude AS DECIMAL(12,8))) " +
	        ") <= 50000",
	        nativeQuery = true)
	    List<Vehicle> findNearbyVehicles(
	            @Param("vehicleType") String vehicleType,
	            @Param("pickupLatitude") String pickupLatitude,
	            @Param("pickupLongitude") String pickupLongitude);



}
