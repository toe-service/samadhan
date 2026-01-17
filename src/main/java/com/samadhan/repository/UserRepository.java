package com.samadhan.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM user_details u
                WHERE u.user_contact_number = :mobile
                   OR u.user_email = :email
            )
            """, nativeQuery = true)
    int existsByMobileOrEmail(
            @Param("mobile") String mobile,
            @Param("email") String email
    );
}




