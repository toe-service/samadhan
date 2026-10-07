package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.samadhan.entity.Vehicle;

public interface VehicleRepository   extends JpaRepository<Vehicle, Long> {

	// Keeps fcm_token unique across vehicles: a token identifies one physical device/app install,
	// and a device only ever has ONE current vehicle session on it, so if this same token is
	// about to be assigned to `keepVehicleId`, any OTHER vehicle row still holding it is stale —
	// that device logged into a different vehicle account since, and the old row's copy was never
	// cleared. Without this, a push correctly targeted at the new vehicle's token would appear to
	// land on the old vehicle too (same device, same token, two DB rows claiming it) — see the
	// Eeco/Scooter/14ft-truck investigation this was built to fix.
	@Modifying
	@javax.transaction.Transactional
	@Query(value = "UPDATE vehicle SET fcm_token = NULL WHERE fcm_token = :fcmToken AND id <> :keepVehicleId", nativeQuery = true)
	void clearFcmTokenFromOtherVehicles(@Param("fcmToken") String fcmToken, @Param("keepVehicleId") Long keepVehicleId);

	@Query(value="select * from vehicle where transfer_id=:vendorId and (is_active IS NULL OR is_active = 1)",nativeQuery=true)
	List<Vehicle> findByVendorId(Long vendorId);

	// Unlike findByVendorId above, includes disabled vehicles — used by the vendor's own fleet
	// management page so a previously disabled vehicle stays visible (with an Enable action)
	// instead of disappearing with no way back.
	@Query(value="select * from vehicle where transfer_id=:vendorId",nativeQuery=true)
	List<Vehicle> findAllByVendorIdIncludingInactive(Long vendorId);

	@Query(value="select * from vehicle where transfer_id=:vendorId and ongoing_status=:isActive and (is_active IS NULL OR is_active = 1)",nativeQuery=true)
	List<Vehicle> findByActiveVendorId(Long vendorId, boolean isActive);

	// LOWER() on the username side only — password stays an exact match, only the login
	// identifier should be case-insensitive.
	@Query(value="select * from vehicle where LOWER(user_name)=LOWER(:userName) and password=:password",nativeQuery=true)
	Vehicle findByUserNamePassword(String userName, String password);

	// LIMIT 1 guards against pre-existing duplicate user_name rows throwing
	// NonUniqueResultException — see the same fix applied to TransferVendorRepository.
	@Query(value="select * from vehicle where LOWER(user_name)=LOWER(:userName) ORDER BY id ASC LIMIT 1",nativeQuery=true)
	Vehicle findByUserName(@Param("userName") String userName);

	// Ownership lookup for VendorAvailabilityServiceImpl#postAvailability/#updateAvailability —
	// confirms a posted vehicleNumber actually belongs to the posting vendor before saving. LIMIT 1
	// + deterministic ORDER BY for the same reason as findByUserName above: vehicle_number has no
	// DB-level unique constraint, so a single-result query needs a tiebreaker against duplicates.
	@Query(value="select * from vehicle where vehicle_number=:vehicleNumber ORDER BY id ASC LIMIT 1",nativeQuery=true)
	Vehicle findByVehicleNumber(@Param("vehicleNumber") String vehicleNumber);


	@Query(value =
	        "SELECT v.* " +
	        "FROM vehicle v " +
	        "LEFT JOIN transfer_vendor tv ON tv.id = v.transfer_id " +
	        "WHERE v.fcm_token IS NOT NULL " +
	        "AND (v.is_active IS NULL OR v.is_active = 1) " +
	        "AND v.ongoing_status = false " +
	        // vendor_vehicle_type holds the enum ordinal (Vehicle.vendorVehicle has no
	        // @Enumerated(EnumType.STRING)), so the caller passes ordinals, not names.
	        // The caller passes the requested vehicle type plus the next larger ones, so a
	        // bigger vehicle standing nearby still gets the offer. Never pass an empty list:
	        // "IN ()" is a syntax error in MySQL.
	        // A request that names no vehicle type (anyVehicleType = 1) drops the filter
	        // entirely, which also lets through vehicles with no type recorded - they would
	        // never match an IN list, since NULL never equals anything.
	        "AND ( :anyVehicleType = 1 OR v.vendor_vehicle_type IN (:vehicleTypes) ) " +
	        "AND v.vehicle_latitude IS NOT NULL " +
	        "AND v.vehicle_longitude IS NOT NULL " +
	        // Long-haul rides (over 100km) are only offered to vendor/fleet vehicles, not
	        // individual (single-vehicle owner-operator) registrants. COALESCE treats a
	        // missing vendor link or an unset is_individual (legacy vendors predating this
	        // flag) as "not individual", so existing vendors keep getting long rides as before.
	        "AND ( :rideDistanceKm <= 100 OR COALESCE(tv.is_individual, false) = false ) " +
	        // Same radius rule as TransferRequestRepository.getVehicleFeed, so a vehicle is
	        // only notified about a ride it would also see in its feed. How near counts as
	        // "nearby" scales with the length of the ride itself: a short local trip is only
	        // worth offering to a vehicle right next to the pickup, while a long-haul ride is
	        // worth a longer drive to reach the pickup point.
	        //   ride  < 20 km  -> vehicle within  3 km
	        //   ride <= 50 km  -> vehicle within 10 km
	        //   ride  < 100 km -> vehicle within 25 km
	        //   ride >= 100 km -> vehicle within 30 km
	        // A ride with no distance_km recorded falls through to 30 km.
	        "AND ST_Distance_Sphere( " +
	        "POINT(CAST(TRIM(v.vehicle_longitude) AS DECIMAL(12,8)), " +
	        "      CAST(TRIM(v.vehicle_latitude) AS DECIMAL(12,8))), " +
	        "POINT(CAST(TRIM(:pickupLongitude) AS DECIMAL(12,8)), " +
	        "      CAST(TRIM(:pickupLatitude) AS DECIMAL(12,8))) " +
	        ") <= CASE " +
	        "      WHEN :rideDistanceKm <  20  THEN  3000 " +
	        "      WHEN :rideDistanceKm <= 50  THEN 10000 " +
	        "      WHEN :rideDistanceKm <  100 THEN 25000 " +
	        "      ELSE 30000 " +
	        "END",
	        nativeQuery = true)
	    List<Vehicle> findNearbyVehicles(
	            @Param("anyVehicleType") int anyVehicleType,
	            @Param("vehicleTypes") List<Integer> vehicleTypes,
	            @Param("pickupLatitude") String pickupLatitude,
	            @Param("pickupLongitude") String pickupLongitude,
	            @Param("rideDistanceKm") Double rideDistanceKm);



}
