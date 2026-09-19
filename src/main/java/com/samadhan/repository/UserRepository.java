package com.samadhan.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.samadhan.entity.UserDetails;

import java.time.LocalDate;
import java.util.List;
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

    // Candidates for AvailableRidesNotificationScheduler: users in the posting's origin city with
    // a registered push token, not already notified today — last_availability_notified_date is
    // the once-per-user-per-day dedupe flag, checked here rather than after the fact so a user
    // matching several routes on the same day only shows up once, on whichever route is processed
    // first.
    @Query(value =
            "SELECT * FROM user_details " +
            "WHERE device_city = :city " +
            "AND fcm_token IS NOT NULL AND fcm_token <> '' " +
            "AND (last_availability_notified_date IS NULL OR last_availability_notified_date < :today)",
            nativeQuery = true)
    List<UserDetails> findUsersToNotifyForCity(@Param("city") String city, @Param("today") LocalDate today);
}




