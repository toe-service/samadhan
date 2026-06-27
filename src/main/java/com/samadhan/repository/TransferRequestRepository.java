package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.TransferRequestDetails;

public interface TransferRequestRepository   extends JpaRepository<TransferRequestDetails, Long> {

	@Query(value="select * from transfer_request_details where user_id=:userId ORDER BY request_created_date DESC" ,nativeQuery = true)
	List<TransferRequestDetails> findTransferRideByUserId(Long userId);

	@Query(value="select * from transfer_request_details where driver_id=:driverId AND transfer_status IN(3,4) ORDER BY request_created_date DESC" ,nativeQuery = true)
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
	
//	@Query(value = "SELECT trd.*, " +
//			"ST_Distance_Sphere( " +
//	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
//	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
//	        ") / 1000 AS distance_km " +
//	        "FROM transfer_request_details trd " +
//	        "JOIN transfer_vendor tv ON tv.id = :vendorId " +
//	        "WHERE " +
//
//	        // Assigned rides
//	        "trd.transfer_id = :vendorId " +
//
//	        "OR (" +
//
//	        // Nearby rides (no restriction on transfer_id)
//	        "trd.source_latitude IS NOT NULL " +
//	        "AND trd.source_longitude IS NOT NULL " +
//
//	        "AND ST_Distance_Sphere( " +
//	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
//	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
//	        ") <= 30000" +
//	        
//			 // Vendor has NOT declined this request
//			 "AND NOT EXISTS ( " +
//			     "SELECT 1 " +
//			     "FROM cancelled_request cr " +
//			     "WHERE cr.transfer_request_id = trd.id " +
//			     "AND cr.vendor_id = :vendorId " +
//			 ") " +
//
//	        ")" +
//	       // "ORDER BY trd.request_created_date DESC",
//	       "ORDER BY " +
//	     //   "CASE WHEN trd.transfer_status = 'COMPLETED' THEN 1 ELSE 0 END ASC, " +
//	        "trd.request_created_date DESC",
//	        nativeQuery = true)
//	List<TransferRequestDetails> showRidestoVendors(Long vendorId);
	
	
	@Query(value =
	        "SELECT trd.*, " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") / 1000 AS distance_km " +
	        "FROM transfer_request_details trd " +
	        "JOIN transfer_vendor tv ON tv.id = :vendorId " +
	        "WHERE tv.vendor_status IN (3,1) " +
	        "AND ( " +

	        // Assigned rides of current vendor
	        "trd.transfer_id = :vendorId " +

	        "OR ( " +

	        // Only unassigned rides
	        "trd.transfer_id IS NULL " +

	        "AND trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +

	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 30000 " +

	        "AND NOT EXISTS ( " +
	        "SELECT 1 FROM cancelled_request cr " +
	        "WHERE cr.transfer_request_id = trd.id " +
	        "AND cr.vendor_id = :vendorId " +
	        ") " +

	        ") " +
	        ") " +
	        "ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> showRidestoVendors(Long vendorId);

	@Query(value="select * from transfer_request_details where vehicle_id=:vehicleId AND transfer_status IN(5,6,7,8) ORDER BY request_created_date DESC " ,nativeQuery = true)
	List<TransferRequestDetails> getRidesByVehicle(Long vehicleId);

	@Query(value="select * from transfer_request_details where id=:transferId AND transfer_id=:vendorId" ,nativeQuery = true)
	Boolean IsExist(Long transferId, Long vendorId);

}
