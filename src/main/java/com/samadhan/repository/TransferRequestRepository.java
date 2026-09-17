package com.samadhan.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.samadhan.entity.TransferRequestDetails;

public interface TransferRequestRepository   extends JpaRepository<TransferRequestDetails, Long> {

	@Query(value="select * from transfer_request_details where user_id=:userId AND (is_deleted IS NULL OR is_deleted = 0) ORDER BY request_created_date DESC" ,nativeQuery = true)
	List<TransferRequestDetails> findTransferRideByUserId(Long userId);

	// Paginated feed backing GET /transfer/rideTransferbyUser/{userId}. statusFilter is one of
	// PENDING (transfer_status = 0), COMPLETED (transfer_status = 8), or OTHER (everything else -
	// ACCEPTED/DECLINED/READYFORPICKUP/HANDOVER/VEHICLEASSIGNED/ONGOING/YETTOBECOMPLETED/CANCELLED).
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.user_id = :userId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "  (:statusFilter = 'PENDING' AND trd.transfer_status = 0) " +
	        "  OR (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'OTHER' AND trd.transfer_status NOT IN (0,8)) " +
	        ") " +
	        "ORDER BY trd.request_created_date DESC",
	        countQuery =
	        "SELECT COUNT(*) FROM transfer_request_details trd " +
	        "WHERE trd.user_id = :userId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "  (:statusFilter = 'PENDING' AND trd.transfer_status = 0) " +
	        "  OR (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'OTHER' AND trd.transfer_status NOT IN (0,8)) " +
	        ")",
	        nativeQuery = true)
	Page<TransferRequestDetails> getUserRidesFeedPaged(
	        @Param("userId") Long userId,
	        @Param("statusFilter") String statusFilter,
	        Pageable pageable);

	@Query(value="select * from transfer_request_details where driver_id=:driverId AND transfer_status IN(3,4) AND (is_deleted IS NULL OR is_deleted = 0) ORDER BY request_created_date DESC" ,nativeQuery = true)
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
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
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

	        "AND ( " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 40000 " +

	        // A fleet vendor's registered address isn't the only thing that counts as "nearby" —
	        // any of their own vehicles sitting near the pickup (e.g. it just finished a
	        // drop-off there) should surface this ride too. Same distance-scales-with-ride-length
	        // rule as TransferRequestRepository.getVehicleFeed / VehicleRepository.findNearbyVehicles.
	        "OR EXISTS ( " +
	        "SELECT 1 FROM vehicle veh " +
	        "WHERE veh.transfer_id = :vendorId " +
	        "AND veh.vehicle_latitude IS NOT NULL AND veh.vehicle_longitude IS NOT NULL " +
	        "AND (veh.is_active IS NULL OR veh.is_active = 1) " +
	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(veh.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(veh.vehicle_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))) " +
	        ") <= CASE " +
	        "WHEN trd.distance_km <  20  THEN  3000 " +
	        "WHEN trd.distance_km <= 50  THEN 10000 " +
	        "WHEN trd.distance_km <  100 THEN 25000 " +
	        "ELSE 30000 " +
	        "END " +
	        ") " +
	        ") " +

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

	// Same eligibility/business-rule filtering as showRidestoVendors above, but paged at the
	// DB level (LIMIT/OFFSET via Pageable) instead of returning every matching row, and with
	// an extra optional status filter for the dashboard's Pending/Accepted/Ongoing tabs.
	// statusFilter is one of ALL/PENDING/ACCEPTED/ONGOING/READYFORPICKUP/HANDOVER/
	// VEHICLEASSIGNED/YETTOBECOMPLETED/COMPLETED/IMMEDIATE/INPROGRESS. ONGOING means literally
	// transfer_status=6 only, matching the dropdown's separate Ready for Pickup/Vehicle Assigned/
	// Handover/Yet to be Completed options — INPROGRESS is the broader READYFORPICKUP(3) +
	// VEHICLEASSIGNED(5) + ONGOING(6) grouping the dashboard's summary tile shows (renamed from
	// "Ongoing" since that name collided with the literal single-status meaning above). IMMEDIATE
	// is restricted to still-PENDING instant bookings — an immediate booking that's already been
	// accepted/assigned belongs under its own current status, not this filter.
	@Query(value =
	        "SELECT trd.*, " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") / 1000 AS distance_km " +
	        "FROM transfer_request_details trd " +
	        "JOIN transfer_vendor tv ON tv.id = :vendorId " +
	        "WHERE tv.vendor_status IN (3,2,5,1) " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "   NOT EXISTS (SELECT 1 FROM vendor_service vs0 WHERE vs0.vendor_id = :vendorId) " +
	        "   OR EXISTS ( " +
	        "      SELECT 1 FROM vendor_service vs " +
	        "      WHERE vs.vendor_id = :vendorId " +
	        "      AND vs.service_type = trd.service_type " +
	        "      AND vs.is_active = 1 " +
	        "   ) " +
	        ") " +
	        "AND ( " +
	        "trd.transfer_id = :vendorId " +
	        "OR ( " +
	        "trd.transfer_status = 0 " +
	        "AND trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +
	        "AND ( " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 40000 " +
	        "OR EXISTS ( " +
	        "SELECT 1 FROM vehicle veh " +
	        "WHERE veh.transfer_id = :vendorId " +
	        "AND veh.vehicle_latitude IS NOT NULL AND veh.vehicle_longitude IS NOT NULL " +
	        "AND (veh.is_active IS NULL OR veh.is_active = 1) " +
	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(veh.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(veh.vehicle_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))) " +
	        ") <= CASE " +
	        "WHEN trd.distance_km <  20  THEN  3000 " +
	        "WHEN trd.distance_km <= 50  THEN 10000 " +
	        "WHEN trd.distance_km <  100 THEN 25000 " +
	        "ELSE 30000 " +
	        "END " +
	        ") " +
	        ") " +
	        "AND NOT EXISTS ( " +
	        "SELECT 1 FROM cancelled_request cr " +
	        "WHERE cr.transfer_request_id = trd.id " +
	        "AND cr.vendor_id = :vendorId " +
	        ") " +
	        ") " +
	        ") " +
	        "AND ( " +
	        "  :statusFilter = 'ALL' " +
	        "  OR (:statusFilter = 'PENDING' AND trd.transfer_status = 0) " +
	        "  OR (:statusFilter = 'ACCEPTED' AND trd.transfer_status = 1) " +
	        "  OR (:statusFilter = 'ONGOING' AND trd.transfer_status = 6) " +
	        "  OR (:statusFilter = 'INPROGRESS' AND trd.transfer_status IN (3,5,6)) " +
	        "  OR (:statusFilter = 'READYFORPICKUP' AND trd.transfer_status = 3) " +
	        "  OR (:statusFilter = 'HANDOVER' AND trd.transfer_status = 4) " +
	        "  OR (:statusFilter = 'VEHICLEASSIGNED' AND trd.transfer_status = 5) " +
	        "  OR (:statusFilter = 'YETTOBECOMPLETED' AND trd.transfer_status = 7) " +
	        "  OR (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'IMMEDIATE' AND trd.instant_booking = 1 AND trd.transfer_status = 0) " +
	        ") " +
	        "AND ( :pickupDate IS NULL OR trd.pickup_date = :pickupDate ) " +
	        "ORDER BY trd.request_created_date DESC",
	        countQuery =
	        "SELECT COUNT(*) " +
	        "FROM transfer_request_details trd " +
	        "JOIN transfer_vendor tv ON tv.id = :vendorId " +
	        "WHERE tv.vendor_status IN (3,2,5,1) " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "   NOT EXISTS (SELECT 1 FROM vendor_service vs0 WHERE vs0.vendor_id = :vendorId) " +
	        "   OR EXISTS ( " +
	        "      SELECT 1 FROM vendor_service vs " +
	        "      WHERE vs.vendor_id = :vendorId " +
	        "      AND vs.service_type = trd.service_type " +
	        "      AND vs.is_active = 1 " +
	        "   ) " +
	        ") " +
	        "AND ( " +
	        "trd.transfer_id = :vendorId " +
	        "OR ( " +
	        "trd.transfer_status = 0 " +
	        "AND trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +
	        "AND ( " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 40000 " +
	        "OR EXISTS ( " +
	        "SELECT 1 FROM vehicle veh " +
	        "WHERE veh.transfer_id = :vendorId " +
	        "AND veh.vehicle_latitude IS NOT NULL AND veh.vehicle_longitude IS NOT NULL " +
	        "AND (veh.is_active IS NULL OR veh.is_active = 1) " +
	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(veh.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(veh.vehicle_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))) " +
	        ") <= CASE " +
	        "WHEN trd.distance_km <  20  THEN  3000 " +
	        "WHEN trd.distance_km <= 50  THEN 10000 " +
	        "WHEN trd.distance_km <  100 THEN 25000 " +
	        "ELSE 30000 " +
	        "END " +
	        ") " +
	        ") " +
	        "AND NOT EXISTS ( " +
	        "SELECT 1 FROM cancelled_request cr " +
	        "WHERE cr.transfer_request_id = trd.id " +
	        "AND cr.vendor_id = :vendorId " +
	        ") " +
	        ") " +
	        ") " +
	        "AND ( " +
	        "  :statusFilter = 'ALL' " +
	        "  OR (:statusFilter = 'PENDING' AND trd.transfer_status = 0) " +
	        "  OR (:statusFilter = 'ACCEPTED' AND trd.transfer_status = 1) " +
	        "  OR (:statusFilter = 'ONGOING' AND trd.transfer_status = 6) " +
	        "  OR (:statusFilter = 'INPROGRESS' AND trd.transfer_status IN (3,5,6)) " +
	        "  OR (:statusFilter = 'READYFORPICKUP' AND trd.transfer_status = 3) " +
	        "  OR (:statusFilter = 'HANDOVER' AND trd.transfer_status = 4) " +
	        "  OR (:statusFilter = 'VEHICLEASSIGNED' AND trd.transfer_status = 5) " +
	        "  OR (:statusFilter = 'YETTOBECOMPLETED' AND trd.transfer_status = 7) " +
	        "  OR (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'IMMEDIATE' AND trd.instant_booking = 1 AND trd.transfer_status = 0) " +
	        ") " +
	        "AND ( :pickupDate IS NULL OR trd.pickup_date = :pickupDate )",
	        nativeQuery = true)
	Page<TransferRequestDetails> showRidestoVendorsPaged(
	        @Param("vendorId") Long vendorId,
	        @Param("statusFilter") String statusFilter,
	        @Param("pickupDate") LocalDate pickupDate,
	        Pageable pageable);

	// Same eligibility filtering as showRidestoVendors, minus any status filter, broken down
	// into the four dashboard buckets in one pass — used so the summary cards always reflect
	// every visible ride, independent of the current page/status filter.
	@Query(value =
	        "SELECT " +
	        "  COUNT(*) AS total, " +
	        "  SUM(CASE WHEN trd.transfer_status = 0 THEN 1 ELSE 0 END) AS pending, " +
	        "  SUM(CASE WHEN trd.transfer_status = 1 THEN 1 ELSE 0 END) AS accepted, " +
	        "  SUM(CASE WHEN trd.transfer_status IN (3,5,6) THEN 1 ELSE 0 END) AS ongoing, " +
	        "  SUM(CASE WHEN trd.pickup_date = CURDATE() THEN 1 ELSE 0 END) AS todayPickup, " +
	        "  SUM(CASE WHEN trd.instant_booking = 1 THEN 1 ELSE 0 END) AS immediateCount " +
	        "FROM transfer_request_details trd " +
	        "JOIN transfer_vendor tv ON tv.id = :vendorId " +
	        "WHERE tv.vendor_status IN (3,2,5,1) " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "   NOT EXISTS (SELECT 1 FROM vendor_service vs0 WHERE vs0.vendor_id = :vendorId) " +
	        "   OR EXISTS ( " +
	        "      SELECT 1 FROM vendor_service vs " +
	        "      WHERE vs.vendor_id = :vendorId " +
	        "      AND vs.service_type = trd.service_type " +
	        "      AND vs.is_active = 1 " +
	        "   ) " +
	        ") " +
	        "AND ( " +
	        "trd.transfer_id = :vendorId " +
	        "OR ( " +
	        "trd.transfer_status = 0 " +
	        "AND trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +
	        "AND ( " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(tv.vendor_longitude) AS DECIMAL(12,8)), CAST(TRIM(tv.vendor_latitude) AS DECIMAL(12,8))) " +
	        ") <= 40000 " +
	        "OR EXISTS ( " +
	        "SELECT 1 FROM vehicle veh " +
	        "WHERE veh.transfer_id = :vendorId " +
	        "AND veh.vehicle_latitude IS NOT NULL AND veh.vehicle_longitude IS NOT NULL " +
	        "AND (veh.is_active IS NULL OR veh.is_active = 1) " +
	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(veh.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(veh.vehicle_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))) " +
	        ") <= CASE " +
	        "WHEN trd.distance_km <  20  THEN  3000 " +
	        "WHEN trd.distance_km <= 50  THEN 10000 " +
	        "WHEN trd.distance_km <  100 THEN 25000 " +
	        "ELSE 30000 " +
	        "END " +
	        ") " +
	        ") " +
	        "AND NOT EXISTS ( " +
	        "SELECT 1 FROM cancelled_request cr " +
	        "WHERE cr.transfer_request_id = trd.id " +
	        "AND cr.vendor_id = :vendorId " +
	        ") " +
	        ") " +
	        ")",
	        nativeQuery = true)
	RideStatusCounts countRidesByStatusForVendor(@Param("vendorId") Long vendorId);

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
	        "WHERE (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
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

	// Pending-only half of getVehicleFeed above (unassigned + nearby, same distance-by-ride-length
	// rule), used by the paginated /rideTransferByVehicle feed's PENDING bucket. Unpaged: the
	// per-ride eligibility filter (vehicle type interchangeability, FCM/availability, individual
	// >100km rule — see TransferRequestServiceImpl.isEligiblePendingRide) still has to run in Java
	// after this query, so pagination on this bucket happens in memory, after filtering.
	@Query(value =
	        "SELECT trd.*, " +
	        "ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(v.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(v.vehicle_latitude) AS DECIMAL(12,8))) " +
	        ") / 1000 AS vehicle_distance_km " +
	        "FROM transfer_request_details trd " +
	        "JOIN vehicle v ON v.id = :vehicleId " +
	        "WHERE trd.vehicle_id IS NULL " +
	        "AND trd.transfer_status = 0 " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND trd.source_latitude IS NOT NULL " +
	        "AND trd.source_longitude IS NOT NULL " +
	        "AND ST_Distance_Sphere( " +
	        "    POINT(CAST(TRIM(trd.source_longitude) AS DECIMAL(12,8)), CAST(TRIM(trd.source_latitude) AS DECIMAL(12,8))), " +
	        "    POINT(CAST(TRIM(v.vehicle_longitude) AS DECIMAL(12,8)), CAST(TRIM(v.vehicle_latitude) AS DECIMAL(12,8))) " +
	        ") <= CASE " +
	        "    WHEN trd.distance_km <  20  THEN  3000 " +
	        "    WHEN trd.distance_km <= 50  THEN 10000 " +
	        "    WHEN trd.distance_km <  100 THEN 25000 " +
	        "    ELSE 30000 " +
	        "END " +
	        "ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> getVehiclePendingFeed(@Param("vehicleId") Long vehicleId);

	// COMPLETED/OTHER halves of the paginated /rideTransferByVehicle feed: rides already assigned
	// to this vehicle, so (unlike the pending bucket above) no post-query eligibility filtering is
	// needed and pagination can happen at the DB level via Pageable/LIMIT-OFFSET.
	// statusFilter is 'COMPLETED' (transfer_status = 8) or 'OTHER' (transfer_status IN
	// (5,6,7) = VEHICLEASSIGNED/ONGOING/YETTOBECOMPLETED — i.e. assigned-but-not-yet-completed).
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.vehicle_id = :vehicleId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "  (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'OTHER' AND trd.transfer_status IN (5,6,7)) " +
	        ") " +
	        "ORDER BY trd.request_created_date DESC",
	        countQuery =
	        "SELECT COUNT(*) FROM transfer_request_details trd " +
	        "WHERE trd.vehicle_id = :vehicleId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "  (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'OTHER' AND trd.transfer_status IN (5,6,7)) " +
	        ")",
	        nativeQuery = true)
	Page<TransferRequestDetails> getVehicleAssignedFeedPaged(
	        @Param("vehicleId") Long vehicleId,
	        @Param("statusFilter") String statusFilter,
	        Pageable pageable);

	@Query(value="select * from transfer_request_details where id=:transferId AND transfer_id=:vendorId" ,nativeQuery = true)
	Boolean IsExist(Long transferId, Long vendorId);

	@Query(value="select count(*) from transfer_request_details where vehicle_id=:vehicleId", nativeQuery = true)
	long countByVehicleId(@Param("vehicleId") Long vehicleId);

	// Backhaul matching candidates: PENDING, unassigned requests picking up on or before the
	// latest expected_date across a vendor's active postings (any earlier pickup date matches
	// too, including "Immediate"/today ones — not just the exact date). Fetched ONCE per
	// VendorAvailabilityServiceImpl#computeMatches call and reused across all of that vendor's
	// postings, with the per-posting lat/lng bounding box and precise endpoint-radius/route-
	// corridor distance checks applied in Java against this shared pool — source_latitude/
	// longitude are TEXT columns, so a CAST/TRIM-based bounding box in SQL can't use an index and
	// is effectively a full scan; running that once per posting (the original approach) multiplied
	// an already-expensive scan by the vendor's active-posting count.
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.transfer_status = 0 " +
	        "AND trd.vehicle_id IS NULL " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND trd.pickup_date <= :maxPickupDate " +
	        "AND trd.source_latitude IS NOT NULL AND trd.source_longitude IS NOT NULL " +
	        "ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> findPendingUnassignedUpTo(@Param("maxPickupDate") LocalDate maxPickupDate);

	// PENDING, unassigned requests whose pickup has already passed without being picked up, so
	// OverduePickupScheduler can re-notify nearby vehicles instead of letting them silently sit
	// forever. Two different "overdue" definitions since pickup_date means different things for
	// each: instant/immediate bookings stamp pickup_date as the day the request was made (see
	// TransferRequestServiceImpl#requestRideTransfer) with the real intent "as soon as possible",
	// so they're judged overdue by elapsed time since creation instead; scheduled bookings are
	// judged overdue once their actual pickup_date has fully passed. overdue_notified_at IS NULL
	// ensures each request is only ever notified once by this scheduler, not on every run.
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.transfer_status = 0 " +
	        "AND trd.vehicle_id IS NULL " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND trd.overdue_notified_at IS NULL " +
	        "AND ( " +
	        "  (trd.instant_booking = 1 AND trd.request_created_date <= :instantBookingCutoff) " +
	        "  OR ((trd.instant_booking IS NULL OR trd.instant_booking = 0) AND trd.pickup_date < :today) " +
	        ")",
	        nativeQuery = true)
	List<TransferRequestDetails> findOverduePendingUnassigned(
	        @Param("today") LocalDate today,
	        @Param("instantBookingCutoff") LocalDateTime instantBookingCutoff);

	// Candidates for OverduePickupScheduler's proactive pre-pickup reminder: still-PENDING,
	// unassigned, non-instant bookings scheduled for today. pickup_schedule is one of a small
	// fixed set of slot labels ("9 AM - 12 PM", etc. — see the live <select> in
	// CreateTransfer/BookVehicle/HomeShifting on the frontend), so the actual "is it within the
	// reminder window" time math is done in Java against that known set rather than in SQL.
	// pickup_reminder_sent_at IS NULL ensures each request is only ever reminded once.
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.transfer_status = 0 " +
	        "AND trd.vehicle_id IS NULL " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND (trd.instant_booking IS NULL OR trd.instant_booking = 0) " +
	        "AND trd.pickup_date = :today " +
	        "AND trd.pickup_reminder_sent_at IS NULL " +
	        "AND trd.pickup_schedule IS NOT NULL",
	        nativeQuery = true)
	List<TransferRequestDetails> findScheduledPendingUnassignedToday(@Param("today") LocalDate today);

}
