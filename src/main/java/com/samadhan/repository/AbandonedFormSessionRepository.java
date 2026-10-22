package com.samadhan.repository;

import com.samadhan.entity.AbandonedFormSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AbandonedFormSessionRepository extends JpaRepository<AbandonedFormSession, Long> {

    Optional<AbandonedFormSession> findByUserContactNumberAndServiceTypeAndResolvedFalse(
            String userContactNumber, String serviceType);

    Page<AbandonedFormSession> findByResolvedFalseOrderByLastUpdatedAtDesc(Pageable pageable);
}
