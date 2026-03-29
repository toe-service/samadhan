package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.TransferRequestDetails;

public interface TransferRequestRepository   extends JpaRepository<TransferRequestDetails, Long> {

	@Query(value="select * from transfer_request_details where user_id=:userId" ,nativeQuery = true)
	List<TransferRequestDetails> findTransferRideByUserId(Long userId);

	@Query(value="select * from transfer_request_details where driver_id=:driverId AND transfer_status IN(3,4)" ,nativeQuery = true)
	List<TransferRequestDetails> findTransferRideByDriverId(Long driverId);

//	@Query(value="SELECT trd.*\r\n"
//			+ "FROM transfer_request_details trd\r\n"
//			+ "JOIN transfer_vendor tv \r\n"
//			+ "    ON 1=1\r\n"
//			+ "WHERE \r\n"
//			+ "    trd.transfer_id = :transferId\r\n"
//			+ "\r\n"
//			+ "    OR (\r\n"
//			+ "        trd.transfer_id IS NULL\r\n"
//			+ "        AND (\r\n"
//			+ "            6371 * ACOS(\r\n"
//			+ "                COS(RADIANS(CAST(tv.vendor_latitude AS DECIMAL(10,6))))\r\n"
//			+ "                * COS(RADIANS(CAST(trd.source_latitude AS DECIMAL(10,6))))\r\n"
//			+ "                * COS(\r\n"
//			+ "                    RADIANS(CAST(trd.source_longitude AS DECIMAL(10,6))) - \r\n"
//			+ "                    RADIANS(CAST(tv.vendor_longitude AS DECIMAL(10,6)))\r\n"
//			+ "                )\r\n"
//			+ "                + SIN(RADIANS(CAST(tv.vendor_latitude AS DECIMAL(10,6))))\r\n"
//			+ "                * SIN(RADIANS(CAST(trd.source_latitude AS DECIMAL(10,6))))\r\n"
//			+ "            )\r\n"
//			+ "        ) <= 50\r\n"
//			+ "    );" ,nativeQuery = true)
//	List<TransferRequestDetails> showRidestoVendors(Long transferId);
	
	@Query(value = "SELECT trd.* " +
	        "FROM transfer_request_details trd " +
	        "JOIN transfer_vendor tv ON tv.id = :vendorId " +
	        "WHERE " +

	        // Assigned rides
	        "trd.transfer_id = :vendorId " +

	        "OR (" +

	        // Nearby rides (no restriction on transfer_id)
	        "trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +

	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 50000" +

	        ")",
	        nativeQuery = true)
	List<TransferRequestDetails> showRidestoVendors(Long vendorId);

	@Query(value="select * from transfer_request_details where vehicle_id=:vehicleId AND transfer_status IN(5,6,7,8) " ,nativeQuery = true)
	List<TransferRequestDetails> getRidesByVehicle(Long vehicleId);

}
