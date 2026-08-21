package com.samadhan.repository;

import com.samadhan.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByUserIdAndUserRole(Long userId, String userRole);

    @Modifying
    void deleteByToken(String token);
}
