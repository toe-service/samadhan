package com.samadhan.repository;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.VendorAvailabilityMatch;

@Repository
public interface VendorAvailabilityMatchRepository extends JpaRepository<VendorAvailabilityMatch, Long> {

	Page<VendorAvailabilityMatch> findByVendorIdOrderByRequestCreatedDateDesc(Long vendorId, Pageable pageable);

	// Same "exact date, or today's Immediate bookings when filtering on today" semantics as the
	// main rides feed and the old live computeMatches — see VendorAvailabilityServiceImpl.
	@Query("SELECT m FROM VendorAvailabilityMatch m WHERE m.vendorId = :vendorId "
			+ "AND (m.pickupDate = :pickupDate OR (:isToday = true AND m.instantBooking = true)) "
			+ "ORDER BY m.requestCreatedDate DESC")
	Page<VendorAvailabilityMatch> findByVendorIdAndPickupDateFiltered(
			@Param("vendorId") Long vendorId,
			@Param("pickupDate") LocalDate pickupDate,
			@Param("isToday") boolean isToday,
			Pageable pageable);

	void deleteByVendorId(Long vendorId);

	void deleteByTransferRequestId(Long transferRequestId);
}
