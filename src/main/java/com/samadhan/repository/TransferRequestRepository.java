package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.TransferRequestDetails;

public interface TransferRequestRepository   extends JpaRepository<TransferRequestDetails, Long> {

	@Query(value="select * from transfer_request_details where user_id=:userId" ,nativeQuery = true)
	List<TransferRequestDetails> findTransferRideByUserId(Long userId);

	@Query(value="select * from transfer_request_details where driver_id=:driverId" ,nativeQuery = true)
	List<TransferRequestDetails> findTransferRideByDriverId(Long driverId);

}
