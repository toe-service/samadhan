package com.samadhan.security;

import com.google.firebase.messaging.FirebaseMessaging;
import com.samadhan.entity.UserDetails;
import com.samadhan.repository.UserRepository;
import com.samadhan.response.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class JwtIntegrationTest {

    @MockBean
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private TokenApi tokenApi;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserRepository userRepository;

    private UserDetails testUser;

    @BeforeEach
    public void setup() {
        testUser = new UserDetails();
        testUser.setUserContactNumber("9876543210");
        testUser.setUserName("Test User");
        testUser.setUserPassword("test@123");
        testUser.setUserRole("USER");
        testUser.setIsActive(true);
        testUser.setUserEmail("test@example.com");
    }

    @Test
    public void testGenerateToken() {
        String token = tokenApi.generateToken("9876543210", 15);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testGenerateTokenWithRole() {
        String token = tokenApi.generateToken("9876543210", "USER", 123L, 15);
        assertNotNull(token);
        assertTrue(tokenApi.validateToken(token));
    }

    @Test
    public void testExtractUsernameFromToken() {
        String token = tokenApi.generateToken("9876543210", "USER", 123L, 15);
        String username = tokenApi.extractUsername(token);
        assertEquals("9876543210", username);
    }

    @Test
    public void testExtractUserIdFromToken() {
        String token = tokenApi.generateToken("9876543210", "USER", 123L, 15);
        Long userId = tokenApi.extractUserId(token);
        assertEquals(123L, userId);
    }

    @Test
    public void testExtractUserRoleFromToken() {
        String token = tokenApi.generateToken("9876543210", "DRIVER", 123L, 15);
        String userRole = tokenApi.extractUserRole(token);
        assertEquals("DRIVER", userRole);
    }

    @Test
    public void testValidateToken() {
        String token = tokenApi.generateToken("9876543210", "USER", 123L, 15);
        assertTrue(tokenApi.validateToken(token));
    }

    @Test
    public void testInvalidToken() {
        assertFalse(tokenApi.validateToken("invalid.token.here"));
    }

    @Test
    public void testBuildUserDetailsFromEntity() {
        org.springframework.security.core.userdetails.UserDetails springUserDetails =
                org.springframework.security.core.userdetails.User.builder()
                        .username("9876543210")
                        .password("test@123")
                        .roles("USER")
                        .build();

        assertNotNull(springUserDetails);
        assertEquals("9876543210", springUserDetails.getUsername());
        assertTrue(springUserDetails.isAccountNonExpired());
        assertTrue(springUserDetails.isAccountNonLocked());
    }

    @Test
    public void testJwtTokenFlow() {
        // Step 1: Generate token for user
        String token = tokenApi.generateToken("9876543210", "USER", 123L, 15);
        assertNotNull(token);

        // Step 2: Validate token
        assertTrue(tokenApi.validateToken(token));

        // Step 3: Extract claims
        String username = tokenApi.extractUsername(token);
        Long userId = tokenApi.extractUserId(token);
        String userRole = tokenApi.extractUserRole(token);

        assertEquals("9876543210", username);
        assertEquals(123L, userId);
        assertEquals("USER", userRole);
    }
}
