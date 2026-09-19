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

	// Backs the public "Available Rides" feed — active postings from today onward, grouped by
	// route+date so a user sees "N vehicles traveling City A -> City B on <date>" instead of one
	// row per vendor. Postings that couldn't resolve a from_city/to_city (see
	// util/CityUtils.extractCity) are excluded rather than shown ungrouped. fromCity/toCity are
	// optional search filters (null = no filter, same permissive-optional-param convention as
	// TransferRequestRepository#showRidestoVendorsPaged's :pickupDate) — exact match, not a
	// substring search. tv.vendor_status IN (3,2,5,1) excludes REJECTED/unverified vendors, same
	// allow-list TransferRequestRepository#showRidestoVendors already uses for the vendor's own
	// feed — a rejected vendor's posting shouldn't be bookable just because Available Rides has
	// its own separate query path.
	@Query(value =
			"SELECT va.from_city AS fromCity, va.to_city AS toCity, va.expected_date AS expectedDate, " +
			"COUNT(*) AS vehicleCount " +
			"FROM vendor_availability va " +
			"JOIN transfer_vendor tv ON tv.id = va.vendor_id " +
			"WHERE va.is_active = 1 AND va.expected_date >= :fromDate " +
			"AND va.from_city IS NOT NULL AND va.to_city IS NOT NULL " +
			"AND tv.vendor_status IN (3,2,5,1) " +
			"AND (:fromCity IS NULL OR va.from_city = :fromCity) " +
			"AND (:toCity IS NULL OR va.to_city = :toCity) " +
			"GROUP BY va.from_city, va.to_city, va.expected_date " +
			"ORDER BY va.expected_date ASC",
			nativeQuery = true)
	List<AvailableRideSummaryProjection> findAvailableRideSummaries(
			@Param("fromDate") LocalDate fromDate,
			@Param("fromCity") String fromCity,
			@Param("toCity") String toCity);

	// Same grouping as findAvailableRideSummaries above, narrowed to exactly one date — used by
	// AvailableRidesNotificationScheduler to get tomorrow's routes (and which origin cities they
	// start from) in one query, before it knows which cities it'll need to look up users for.
	// Same vendor_status allow-list as findAvailableRideSummaries — a rejected vendor's route
	// shouldn't trigger a "shift your goods" notification either.
	@Query(value =
			"SELECT va.from_city AS fromCity, va.to_city AS toCity, va.expected_date AS expectedDate, " +
			"COUNT(*) AS vehicleCount " +
			"FROM vendor_availability va " +
			"JOIN transfer_vendor tv ON tv.id = va.vendor_id " +
			"WHERE va.is_active = 1 AND va.expected_date = :onDate " +
			"AND va.from_city IS NOT NULL AND va.to_city IS NOT NULL " +
			"AND tv.vendor_status IN (3,2,5,1) " +
			"GROUP BY va.from_city, va.to_city, va.expected_date",
			nativeQuery = true)
	List<AvailableRideSummaryProjection> findAvailableRideSummariesOnDate(@Param("onDate") LocalDate onDate);

	// Individual (ungrouped) active postings for one route+date, with the owning vendor's display
	// name joined in directly — backs the "pick a specific vendor" drill-down after a route
	// summary is tapped in the "Available Rides" tab. Native SQL + a JOIN (not the entity's own
	// lazy transferVendor relation) so the vendor name is fetched in this one query rather than
	// risking a LazyInitializationException outside a transaction. Same vendor_status allow-list
	// as findAvailableRideSummaries, so a rejected vendor can't be picked from this list either.
	@Query(value =
			"SELECT va.id AS availabilityId, va.vendor_id AS vendorId, tv.vendor_name AS vendorName, " +
			"va.vehicle_type AS vehicleType, va.vehicle_category AS vehicleCategory, " +
			"va.expected_date AS expectedDate " +
			"FROM vendor_availability va " +
			"JOIN transfer_vendor tv ON tv.id = va.vendor_id " +
			"WHERE va.is_active = 1 AND va.expected_date = :onDate " +
			"AND va.from_city = :fromCity AND va.to_city = :toCity " +
			"AND tv.vendor_status IN (3,2,5,1) " +
			"ORDER BY va.created_at ASC",
			nativeQuery = true)
	List<AvailablePostingProjection> findAvailablePostings(
			@Param("fromCity") String fromCity, @Param("toCity") String toCity, @Param("onDate") LocalDate onDate);
}
