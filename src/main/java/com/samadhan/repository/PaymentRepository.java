package com.samadhan.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.Subscription;

@Repository
public interface PaymentRepository  extends JpaRepository<Subscription, Long>{

	@Query(value="select * from subscription where vendor_id=:vendorId" ,nativeQuery = true)
	Subscription findByVendorId(Long vendorId);

	@Query(value="select * from subscription where end_date<:today" ,nativeQuery = true)
	List<Subscription> findByEndDateBeforeAndStatus(@Param("today") LocalDate today);

}
