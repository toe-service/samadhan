package com.samadhan.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.samadhan.entity.CancelledRequest;

public interface CancelledRequestRepository  extends JpaRepository<CancelledRequest, Long>{

	@Modifying
	@Transactional
	@Query("DELETE FROM CancelledRequest c WHERE c.transferRequest.id = :transferId")
	void deleteByTransferRequest(Long transferId);

}
