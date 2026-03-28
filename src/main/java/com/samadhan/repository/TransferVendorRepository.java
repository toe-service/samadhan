package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.TransferVendor;

@Repository
public interface TransferVendorRepository extends JpaRepository<TransferVendor, Long> {

	@Query(value="select * from transfer_vendor where vendor_email=:userName AND vendor_password=:password" ,nativeQuery = true)
	TransferVendor findByUserAndPassword(String userName, String password);
	
}


