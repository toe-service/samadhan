package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samadhan.entity.CancelledRequest;

public interface CancelledRequestRepository  extends JpaRepository<CancelledRequest, Long>{

}
