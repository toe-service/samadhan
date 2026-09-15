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

	// All active postings system-wide, regardless of vendor — used when a single new/re-entered
	// pending request needs to be checked against every vendor's postings (the inverse direction
	// of the per-vendor recompute above), see VendorAvailabilityServiceImpl#recomputeForRequest.
	List<VendorAvailability> findByActiveTrueOrderByExpectedDateAsc();

	// Vendor IDs with at least one active posting — used only to seed vendor_availability_match
	// once at startup if it's ever empty, see VendorAvailabilityServiceImpl#bootstrapMatchesIfEmpty.
	@Query("SELECT DISTINCT va.transferVendor.id FROM VendorAvailability va WHERE va.active = true")
	List<Long> findDistinctVendorIdByActiveTrue();

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
