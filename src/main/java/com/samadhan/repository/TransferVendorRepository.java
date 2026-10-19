package com.samadhan.repository;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.samadhan.entity.TransferVendor;

@Repository
public interface TransferVendorRepository extends JpaRepository<TransferVendor, Long> {

	// LIMIT 1 + a deterministic ORDER BY guards against pre-existing duplicate vendor_email rows
	// (there's no DB-level unique constraint on vendor_email) throwing NonUniqueResultException —
	// a single-result native query otherwise fails hard the moment 2+ rows match. New
	// registrations can no longer create duplicates (see TransferVendorServiceImpl#registerVendor),
	// but this keeps existing lookups (login, forgot-password) working for any that predate that
	// check, consistently resolving to the oldest (original) account.
	@Query(value="select * from transfer_vendor where vendor_email=:userName AND vendor_password=:password ORDER BY id ASC LIMIT 1" ,nativeQuery = true)
	TransferVendor findByUserAndPassword(String userName, String password);

	// Used for the hashed-password login path (see LoginService) — looks up by email only, then
	// the password itself is verified in Java via PasswordEncoder.matches(), not in SQL, since a
	// bcrypt hash can't be compared with a plain "=". LOWER() on both sides so login/registration/
	// forgot-password all treat "Name@Example.com" and "name@example.com" as the same account,
	// regardless of the DB column's collation.
	@Query(value="select * from transfer_vendor where LOWER(vendor_email)=LOWER(:userName) ORDER BY id ASC LIMIT 1" ,nativeQuery = true)
	TransferVendor findByVendorEmail(String userName);

//	@Modifying
//	@Transactional  
//	@Query(
//	 value =
//	 "UPDATE transfer_vendor " +
//	 "SET vendor_status =: vendorStatus " +
//	 "WHERE id = :vendorId",
//	 nativeQuery = true)
//	void activateVendor(Long vendorId, int vendorStatus);
	
	@Modifying
	@Transactional
	@Query(
	    value = "UPDATE transfer_vendor " +
	            "SET vendor_status = :vendorStatus " +
	            "WHERE id = :vendorId",
	    nativeQuery = true
	)
	void activateVendor(
	    @Param("vendorId") Long vendorId,
	    @Param("vendorStatus") int vendorStatus
	);

	@Query(
		    value = "Select * from transfer_vendor ",
		    nativeQuery = true
		)
	List<TransferVendor> findAllActiveVendors();

	// Public profile lookup by slug (used for the /vendor/<slug> shareable page) — the frontend
	// builds the slug by lowercasing the vendor name and stripping spaces (e.g. "Kaka Logistics"
	// -> "kakalogistics"), so the match here strips spaces from vendor_name the same way before
	// comparing. Same LIMIT 1 + deterministic ORDER BY guard as findByVendorEmail — there's no
	// DB-level unique constraint on vendor_name either, so a single-result native query would
	// otherwise fail hard the moment 2+ vendors collide on the same slug.
	@Query(value="select * from transfer_vendor where lower(replace(vendor_name, ' ', ''))=lower(:vendorSlug) ORDER BY id ASC LIMIT 1" ,nativeQuery = true)
	TransferVendor findByVendorNameSlug(String vendorSlug);

	boolean existsByVendorNameIgnoreCase(String vendorName);

	// Admin dashboard summary card counts — platform-wide, no single-vendor scoping (see
	// AdminVendorStatusCounts for what each bucket means).
	@Query(value =
			"SELECT " +
			"  COUNT(*) AS totalVendors, " +
			"  SUM(CASE WHEN vendor_status IN (1,3) THEN 1 ELSE 0 END) AS onboardedVendors, " +
			"  SUM(CASE WHEN vendor_status IN (0,2) THEN 1 ELSE 0 END) AS pendingVendors, " +
			"  SUM(CASE WHEN vendor_status = 4 THEN 1 ELSE 0 END) AS rejectedVendors, " +
			"  SUM(CASE WHEN vendor_status = 5 THEN 1 ELSE 0 END) AS suspendedVendors " +
			"FROM transfer_vendor",
			nativeQuery = true)
	AdminVendorStatusCounts countVendorsByStatus();

	// Admin vendor list: one row per vendor with vehicle/agent/pending-request counts via
	// subqueries, so the frontend doesn't need a separate round-trip per vendor. Pending-request
	// count only counts requests actually assigned to this vendor (transfer_id = tv.id) — not the
	// complex geo-eligibility "available feed" matching countRidesByStatusForVendor does, which
	// answers a different question (what could this vendor pick up) than what an admin wants here
	// (what is this vendor actually on the hook for). walletBalance is a LEFT JOIN (not every
	// vendor necessarily has a vendor_wallet row yet) defaulted to 0 via COALESCE so the admin
	// panel never has to special-case a missing wallet. search is optional — blank/null matches
	// everyone, used both for the plain paginated list and the Assign-modal's vendor-name lookup.
	@Query(value =
			"SELECT tv.id AS vendorId, tv.vendor_name AS vendorName, tv.vendor_email AS vendorEmail, " +
			"  tv.vendor_status AS vendorStatus, tv.is_individual AS isIndividual, " +
			"  COALESCE(vw.balance, 0) AS walletBalance, " +
			"  (SELECT COUNT(*) FROM vehicle v WHERE v.transfer_id = tv.id) AS vehicleCount, " +
			"  (SELECT COUNT(*) FROM driver d WHERE d.transfer_id = tv.id) AS agentCount, " +
			"  (SELECT COUNT(*) FROM transfer_request_details trd WHERE trd.transfer_id = tv.id " +
			"     AND trd.transfer_status = 0 AND (trd.is_deleted IS NULL OR trd.is_deleted = 0)" +
			"  ) AS pendingRequestCount " +
			"FROM transfer_vendor tv " +
			"LEFT JOIN vendor_wallet vw ON vw.vendor_id = tv.id " +
			"WHERE (:search IS NULL OR :search = '' OR LOWER(tv.vendor_name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
			"ORDER BY tv.id DESC",
			countQuery =
			"SELECT COUNT(*) FROM transfer_vendor tv " +
			"WHERE (:search IS NULL OR :search = '' OR LOWER(tv.vendor_name) LIKE LOWER(CONCAT('%', :search, '%')))",
			nativeQuery = true)
	Page<AdminVendorSummary> findAllVendorSummaries(@Param("search") String search, Pageable pageable);

}


