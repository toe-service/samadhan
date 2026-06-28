package com.samadhan.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import com.samadhan.entity.TransferVendor;

@Repository
public interface TransferVendorRepository extends JpaRepository<TransferVendor, Long> {

	@Query(value="select * from transfer_vendor where vendor_email=:userName AND vendor_password=:password" ,nativeQuery = true)
	TransferVendor findByUserAndPassword(String userName, String password);

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
	
}


