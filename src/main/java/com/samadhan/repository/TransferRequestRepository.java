package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
	        "WHERE tv.vendor_status IN (3,2,5,1) " +
	        "AND ( " +

	        // Assigned rides of current vendor
	        
	        "   NOT EXISTS (SELECT 1 FROM vendor_service vs0 WHERE vs0.vendor_id = :vendorId) " +
	        "   OR EXISTS ( " +
	                // Assigned rides of current vendor
	        "      SELECT 1 FROM vendor_service vs " +
	        "      WHERE vs.vendor_id = :vendorId " +
	        "      AND vs.service_type = trd.service_type " +
	        "      AND vs.is_active = 1 " +
	        "   ) " +
	        ") " +
	        "AND ( " +
	        
	        "trd.transfer_id = :vendorId " +

	        "OR ( " +

	        // Only pending rides
	        "trd.transfer_status = 0 " +

	        "AND trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +

	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 40000 " +

	        "AND NOT EXISTS ( " +
	        "SELECT 1 FROM cancelled_request cr " +
	        "WHERE cr.transfer_request_id = trd.id " +
	        "AND cr.vendor_id = :vendorId " +
	        ") " +

	        ") " +
	        ") " +
	        
//			 // Business Rules
//			 "AND ( " +
//			 "   trd.service_type = 'BOOKVEHICLE' " +
//			 "   OR trd.service_type = 'HOMESHIFTING' " +
//			 "   OR ( " +
//			 "       trd.service_type = 'TRANSFERSERVICE' " +
//			 "       AND ( " +
//			 "           trd.parcel_type IN ('Bike','Car') " +
//			 "           OR ( " +
//			 "               trd.parcel_type = 'Package' " +
//			 "               AND trd.distance_km >= 30 " +
//			 "           ) " +
//			 "       ) " +
//			 "   ) " +
//			 ") " +
	        
	        "ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> showRidestoVendors(Long vendorId);

//	@Query(value =
//	        "SELECT * " +
//	        "FROM transfer_request_details t " +
//	        "WHERE " +
//	        "  ( t.vehicle_id = :vehicleId AND t.transfer_status IN (5,6,7,8) ) " +
//	        "  OR " +
//	        "  ( t.transfer_status = 'PENDING' " +
//	        "    AND t.pickup_latitude IS NOT NULL " +
//	        "    AND t.pickup_longitude IS NOT NULL " +
//	        "    AND ST_Distance_Sphere( " +
//	        "          POINT(CAST(TRIM(t.pickup_longitude) AS DECIMAL(12,8)), " +
//	        "                CAST(TRIM(t.pickup_latitude) AS DECIMAL(12,8))), " +
//	        "          POINT(CAST(:vehicleLongitude AS DECIMAL(12,8)), " +
//	        "                CAST(:vehicleLatitude AS DECIMAL(12,8))) " +
//	        "        ) <= 30000 " +
//	        "  ) " +
//	        "ORDER BY t.request_created_date DESC",
//	        nativeQuery = true)
//	List<TransferRequestDetails> findVehicleFeed(
//	        @Param("vehicleId") Long vehicleId,
//	        @Param("vehicleLatitude") String vehicleLatitude,
//	        @Param("vehicleLongitude") String vehicleLongitude);
	
	@Query(value =
	        "SELECT trd.*, " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(v.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(v.vehicle_latitude) AS DECIMAL(12,8))) " +
	        ") / 1000 AS vehicle_distance_km " +
	        "FROM transfer_request_details trd " +
	        "JOIN vehicle v ON v.id = :vehicleId " +
	        "WHERE ( " +
	        // Already assigned to this vehicle, in an active status
	        "   ( trd.vehicle_id = :vehicleId AND trd.transfer_status IN (5,6,7,8) ) " +
	        "   OR ( " +
	        // Unassigned and nearby. How near counts as "nearby" scales with the
	        // length of the ride itself: a short local trip is only worth showing
	        // to a vehicle right next to the pickup, while a long-haul ride is
	        // worth a longer drive to reach the pickup point.
	        //   ride  < 20 km  -> vehicle within  3 km
	        //   ride <= 50 km  -> vehicle within 10 km
	        //   ride  < 100 km -> vehicle within 25 km
	        //   ride >= 100 km -> vehicle within 30 km
	        // Rides with no distance_km recorded fall through to 30 km, which is
	        // the radius the feed used before this rule existed.
	        "       trd.vehicle_id IS NULL " +
	        "       AND trd.source_latitude IS NOT NULL " +
	        "       AND trd.source_longitude IS NOT NULL " +
	        "       AND ST_Distance_Sphere( " +
	        "           POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "           POINT(CAST(TRIM(v.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(v.vehicle_latitude) AS DECIMAL(12,8))) " +
	        "       ) <= CASE " +
	        "           WHEN trd.distance_km <  20  THEN  3000 " +
	        "           WHEN trd.distance_km <= 50  THEN 10000 " +
	        "           WHEN trd.distance_km <  100 THEN 25000 " +
	        "           ELSE 30000 " +
	        "       END " +
	        "   ) " +
	        ") " +
	        "ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> getVehicleFeed(@Param("vehicleId") Long vehicleId);

	@Query(value="select * from transfer_request_details where id=:transferId AND transfer_id=:vendorId" ,nativeQuery = true)
	Boolean IsExist(Long transferId, Long vendorId);

}
