package com.samadhan.repository;

import java.util.List;

import javax.transaction.Transactional;

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
	// bcrypt hash can't be compared with a plain "=".
	@Query(value="select * from transfer_vendor where vendor_email=:userName ORDER BY id ASC LIMIT 1" ,nativeQuery = true)
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
	
}


