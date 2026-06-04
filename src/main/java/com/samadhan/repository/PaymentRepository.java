package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.Payment;

@Repository
public interface PaymentRepository  extends JpaRepository<Payment, Long>{

	@Query(value="select * from payment where vendor_id=:vendorId" ,nativeQuery = true)
	Payment findByVendorId(Long vendorId);

}
