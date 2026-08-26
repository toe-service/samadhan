package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.UserDetails;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDetails, Long> {

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

    Optional<UserDetails> findByUserContactNumber(String mobileNumber);

    @Query(value = "Select * from user_details where user_email = :userName AND user_password =:password", nativeQuery = true)
	UserDetails findByUserNamePassword(String userName, String password);

    // LIMIT 1 guards against pre-existing duplicate user_email rows throwing
    // NonUniqueResultException — see the same fix applied to TransferVendorRepository.
    @Query(value = "Select * from user_details where user_email = :userEmail ORDER BY id ASC LIMIT 1", nativeQuery = true)
    UserDetails findByUserEmail(@Param("userEmail") String userEmail);
}




