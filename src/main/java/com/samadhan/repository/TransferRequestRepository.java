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
	// PENDING (transfer_status = 0), COMPLETED (transfer_status = 8), OTHER (everything else -
	// ACCEPTED/DECLINED/READYFORPICKUP/HANDOVER/VEHICLEASSIGNED/ONGOING/YETTOBECOMPLETED/CANCELLED),
	// or ACTIVE (every status except COMPLETED — i.e. PENDING + OTHER combined in one bucket, for
	// the user dashboard's "everything except completed" view).
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.user_id = :userId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND ( " +
	        "  (:statusFilter = 'PENDING' AND trd.transfer_status = 0) " +
	        "  OR (:statusFilter = 'COMPLETED' AND trd.transfer_status = 8) " +
	        "  OR (:statusFilter = 'OTHER' AND trd.transfer_status NOT IN (0,8)) " +
	        "  OR (:statusFilter = 'ACTIVE' AND trd.transfer_status != 8) " +
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
	        "  OR (:statusFilter = 'ACTIVE' AND trd.transfer_status != 8) " +
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

	        // Only pending rides. transfer_id IS NULL excludes a request already targeted to a
	        // specific vendor (see requestRideTransfer's "Available Rides" flow — pre-set vendor,
	        // deliberately left PENDING) from this open/nearby broadcast branch; it's still
	        // visible to its own targeted vendor via the "trd.transfer_id = :vendorId" branch
	        // above regardless of status. A no-op for every pre-existing PENDING row, since
	        // transfer_id was never set while status stayed PENDING before that flow existed.
	        "trd.transfer_status = 0 " +
	        "AND trd.transfer_id IS NULL " +

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
	        "AND trd.transfer_id IS NULL " +
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
	        "AND trd.transfer_id IS NULL " +
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
	        "AND trd.transfer_id IS NULL " +
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
	        "       AND trd.transfer_id IS NULL " +
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
	        "AND trd.transfer_id IS NULL " +
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
	        "AND trd.transfer_id IS NULL " +
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
	        "AND trd.transfer_id IS NULL " +
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
	        "AND trd.transfer_id IS NULL " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "AND (trd.instant_booking IS NULL OR trd.instant_booking = 0) " +
	        "AND trd.pickup_date = :today " +
	        "AND trd.pickup_reminder_sent_at IS NULL " +
	        "AND trd.pickup_schedule IS NOT NULL",
	        nativeQuery = true)
	List<TransferRequestDetails> findScheduledPendingUnassignedToday(@Param("today") LocalDate today);

	// Feeds the vendor dashboard's notification bell (NotificationProvider.jsx polls
	// GET /transfer/pickupRemindersDueSoon) — requests whose pre-pickup reminder
	// (OverduePickupScheduler#sendPrePickupReminders, same pickup_reminder_sent_at column above)
	// was just pushed to nearby vehicles. Still-open to any eligible vendor (not yet claimed —
	// same transfer_status/vehicle_id/transfer_id gate as findScheduledPendingUnassignedToday), so
	// every vendor polling this sees the same "pickup coming up, still unclaimed" set their own
	// nearby vehicles were just pushed, right in their own dashboard.
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.pickup_reminder_sent_at IS NOT NULL " +
	        "AND trd.pickup_reminder_sent_at >= :since " +
	        "AND trd.transfer_status = 0 " +
	        "AND trd.vehicle_id IS NULL " +
	        "AND trd.transfer_id IS NULL " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "ORDER BY trd.pickup_reminder_sent_at DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> findRecentPickupReminders(@Param("since") LocalDateTime since);

	// Same shape as findRecentPickupReminders above, but for OverduePickupScheduler#
	// notifyOverduePickups instead — feeds GET /transfer/overduePickupsDueSoon, which surfaces an
	// already-overdue, still-unclaimed request on the vendor dashboard's own notification bell.
	// This is now the ONLY place an overdue pickup is surfaced — notifyOverduePickups no longer
	// pushes to vehicle devices for this (see that method's comment for why: overdue pickups are a
	// vendor-side concern, not something that should wake up every nearby vehicle's app again).
	@Query(value =
	        "SELECT * FROM transfer_request_details trd " +
	        "WHERE trd.overdue_notified_at IS NOT NULL " +
	        "AND trd.overdue_notified_at >= :since " +
	        "AND trd.transfer_status = 0 " +
	        "AND trd.vehicle_id IS NULL " +
	        "AND trd.transfer_id IS NULL " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) " +
	        "ORDER BY trd.overdue_notified_at DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> findRecentOverduePickups(@Param("since") LocalDateTime since);

	// Per-vehicle summary for the vendor dashboard's Fleet Performance page: current job (if any),
	// how many rides this vehicle has completed/has in progress, how much ride revenue it's
	// brought in, and how much platform fee those completed rides cost the vendor's wallet
	// ("Ride Acceptance/Start/Completion Fee" rows in wallet_transaction, joined back via
	// transfer_request_id since that table has no vehicle_id of its own). Ongoing = statuses
	// 3/4/5/6/7 (ReadyForPickup/Handover/VehicleAssigned/Transferring/YetToBeCompleted); Completed
	// = 8 (see rideStatusEnum). One row per vehicle in the vendor's fleet, including idle ones
	// with zero rides (LEFT JOIN), so the page always reflects the whole fleet, not just
	// vehicles that have done at least one job.
	@Query(value =
	        "SELECT v.id AS vehicleId, v.vehicle_number AS vehicleNumber, v.vendor_vehicle_type AS vehicleType, " +
	        "v.vehicle_category AS vehicleCategory, v.is_active AS isActive, v.avg_rating AS avgRating, " +
	        "v.rating_count AS ratingCount, " +
	        "MAX(CASE WHEN t.transfer_status IN (3,4,5,6,7) THEN t.id END) AS currentRideId, " +
	        "COALESCE(SUM(CASE WHEN t.transfer_status = 8 THEN 1 ELSE 0 END), 0) AS completedRides, " +
	        "COALESCE(SUM(CASE WHEN t.transfer_status IN (3,4,5,6,7) THEN 1 ELSE 0 END), 0) AS ongoingRides, " +
	        "COALESCE(SUM(CASE WHEN t.transfer_status = 8 THEN t.ride_cost ELSE 0 END), 0) AS totalRevenue, " +
	        "COALESCE((SELECT SUM(wt.amount) FROM wallet_transaction wt " +
	        "          WHERE wt.transfer_request_id IN ( " +
	        "              SELECT t3.id FROM transfer_request_details t3 " +
	        "              WHERE t3.vehicle_id = v.id AND t3.transfer_status = 8 " +
	        "          ) " +
	        "          AND wt.transaction_type IN ('Ride Acceptance Fee','Ride Start Fee','Ride Completion Fee') " +
	        "         ), 0) AS totalPlatformFee " +
	        "FROM vehicle v " +
	        "LEFT JOIN transfer_request_details t ON t.vehicle_id = v.id " +
	        "WHERE v.transfer_id = :vendorId " +
	        "GROUP BY v.id, v.vehicle_number, v.vendor_vehicle_type, v.vehicle_category, v.is_active, " +
	        "v.avg_rating, v.rating_count " +
	        "ORDER BY v.id",
	        nativeQuery = true)
	List<VehiclePerformanceProjection> findVehiclePerformanceByVendor(@Param("vendorId") Long vendorId);

	// Same shape as findVehiclePerformanceByVendor above, but for agents: "ride value" is the
	// total ride_cost of jobs they handed off (agents aren't separately wallet-charged -- the
	// platform fee is already counted once against the vehicle that completed the job), and
	// ongoing covers from the moment they're assigned (HANDOVER=4) through completion.
	@Query(value =
	        "SELECT d.id AS driverId, d.driver_name AS driverName, d.driver_contact_number AS driverContactNumber, " +
	        "d.is_active AS isActive, " +
	        "MAX(CASE WHEN t.transfer_status IN (4,5,6,7) THEN t.id END) AS currentRideId, " +
	        "COALESCE(SUM(CASE WHEN t.transfer_status = 8 THEN 1 ELSE 0 END), 0) AS completedHandoffs, " +
	        "COALESCE(SUM(CASE WHEN t.transfer_status IN (4,5,6,7) THEN 1 ELSE 0 END), 0) AS ongoingHandoffs, " +
	        "COALESCE(SUM(CASE WHEN t.transfer_status = 8 THEN t.ride_cost ELSE 0 END), 0) AS totalRideValue " +
	        "FROM driver d " +
	        "LEFT JOIN transfer_request_details t ON t.driver_id = d.id " +
	        "WHERE d.transfer_id = :vendorId " +
	        "GROUP BY d.id, d.driver_name, d.driver_contact_number, d.is_active " +
	        "ORDER BY d.id",
	        nativeQuery = true)
	List<AgentPerformanceProjection> findAgentPerformanceByVendor(@Param("vendorId") Long vendorId);

	// Full ride history for one vehicle/agent -- backs the drill-down view from the Fleet/Team
	// Performance page (tap a vehicle or agent to see every job it's ever done, not just the
	// aggregate counts above).
	// Vendor ownership enforced in the query itself (not just the controller) -- the vehicle must
	// belong to :vendorId, so one vendor can't read another vendor's fleet history by guessing ids.
	@Query(value = "SELECT trd.* FROM transfer_request_details trd " +
	        "JOIN vehicle v ON v.id = trd.vehicle_id " +
	        "WHERE trd.vehicle_id = :vehicleId AND v.transfer_id = :vendorId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> findByVehicleIdOrderByRequestCreatedDateDesc(
	        @Param("vehicleId") Long vehicleId, @Param("vendorId") Long vendorId);

	@Query(value = "SELECT trd.* FROM transfer_request_details trd " +
	        "JOIN driver d ON d.id = trd.driver_id " +
	        "WHERE trd.driver_id = :driverId AND d.transfer_id = :vendorId " +
	        "AND (trd.is_deleted IS NULL OR trd.is_deleted = 0) ORDER BY trd.request_created_date DESC",
	        nativeQuery = true)
	List<TransferRequestDetails> findByDriverIdOrderByRequestCreatedDateDesc(
	        @Param("driverId") Long driverId, @Param("vendorId") Long vendorId);

}
