package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.Subscription;

@Repository
public interface PaymentRepository  extends JpaRepository<Subscription, Long>{

	@Query(value="select * from subscription where vendor_id=:vendorId" ,nativeQuery = true)
	Subscription findByVendorId(Long vendorId);

}
