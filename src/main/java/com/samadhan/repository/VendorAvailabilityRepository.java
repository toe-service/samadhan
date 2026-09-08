package com.samadhan.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.VendorAvailability;

@Repository
public interface VendorAvailabilityRepository extends JpaRepository<VendorAvailability, Long> {

	List<VendorAvailability> findByTransferVendorIdAndActiveTrueOrderByExpectedDateAsc(Long vendorId);

	// Matches requests to vendors who declared they'll be near a given pickup point on the
	// request's pickup date (or "today" for instant bookings), within the same 40km radius
	// showRidestoVendors already uses for a vendor's permanent location.
	@Query(value =
			"SELECT va.* FROM vendor_availability va " +
			"WHERE va.is_active = 1 " +
			"AND va.expected_date = :onDate " +
			"AND va.to_latitude IS NOT NULL AND va.to_longitude IS NOT NULL " +
			"AND ST_Distance_Sphere( " +
			"  POINT(CAST(TRIM(va.to_longitude) AS DECIMAL(12,8)), CAST(TRIM(va.to_latitude) AS DECIMAL(12,8))), " +
			"  POINT(:pickupLongitude, :pickupLatitude) " +
			") <= 40000",
			nativeQuery = true)
	List<VendorAvailability> findMatchingAvailability(
			@Param("pickupLatitude") double pickupLatitude,
			@Param("pickupLongitude") double pickupLongitude,
			@Param("onDate") LocalDate onDate);
}
