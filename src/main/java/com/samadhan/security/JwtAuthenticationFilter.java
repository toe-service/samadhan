package com.samadhan.security;

import com.samadhan.entity.Driver;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.UserRole;
import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.UserRepository;
import com.samadhan.repository.VehicleRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private TokenApi tokenApi;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private TransferVendorRepository transferVendorRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && tokenApi.validateToken(jwt) && !isDisabledDriverOrVehicle(jwt)
                    && !isSessionInvalid(jwt)) {
                String username = tokenApi.extractUsername(jwt);
                String userRole = tokenApi.extractUserRole(jwt);

                GrantedAuthority authority = new SimpleGrantedAuthority(
                        "ROLE_" + (userRole != null ? userRole : "USER"));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, Collections.singletonList(authority));
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException ex) {
            logger.debug("JWT token is expired: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.debug("JWT claims string is empty: " + ex.getMessage());
        } catch (Exception ex) {
            logger.debug("Could not set user authentication: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // Re-checks isActive against the DB on every request for driver/vehicle-role tokens, so a
    // disabled driver/vehicle is cut off immediately instead of waiting for its access token to
    // expire naturally. USER/VENDOR tokens skip this (no added DB call) since disabling isn't
    // exposed for those roles today.
    private boolean isDisabledDriverOrVehicle(String jwt) {
        String userRole = tokenApi.extractUserRole(jwt);
        Long userId = tokenApi.extractUserId(jwt);
        if (userId == null) {
            return false;
        }

        if (UserRole.DRIVER.getValue().equalsIgnoreCase(userRole)) {
            Driver driver = driverRepository.findById(userId).orElse(null);
            return driver != null && Boolean.FALSE.equals(driver.getIsActive());
        }

        if (UserRole.VEHICLE.getValue().equalsIgnoreCase(userRole)) {
            Vehicle vehicle = vehicleRepository.findById(userId).orElse(null);
            return vehicle != null && Boolean.FALSE.equals(vehicle.getIsActive());
        }

        return false;
    }

    // Single-active-session enforcement for VENDOR/USER/VEHICLE: this token's "jti" (set at login
    // to a fresh random value — see TokenApi#generateToken(..., sessionId)) must still match the
    // value currently stored on the account. A later login anywhere overwrites that stored value,
    // so every token from this (now-superseded) login starts failing this check on its very next
    // use — immediate, not "once it naturally expires." DRIVER is deliberately not covered here
    // (only the three roles this was asked for); a token missing/predating this feature (no jti,
    // or an account whose currentSessionId was never set) fails open — stored null can't match a
    // real jti — which would incorrectly reject valid older tokens if this were deployed without
    // a backfill, but for a brand-new column every account's stored value is null from the same
    // moment every existing token's jti is still "admin" (the old shared default), so this only
    // starts actually mattering from each account's next fresh login onward.
    private boolean isSessionInvalid(String jwt) {
        String userRole = tokenApi.extractUserRole(jwt);
        Long userId = tokenApi.extractUserId(jwt);
        if (userId == null) {
            return false;
        }

        String tokenSessionId = tokenApi.extractJti(jwt);

        if (UserRole.VENDOR.getValue().equalsIgnoreCase(userRole)) {
            TransferVendor vendor = transferVendorRepository.findById(userId).orElse(null);
            return vendor != null && vendor.getCurrentSessionId() != null
                    && !vendor.getCurrentSessionId().equals(tokenSessionId);
        }

        if (UserRole.USER.getValue().equalsIgnoreCase(userRole)) {
            UserDetails user = userRepository.findById(userId).orElse(null);
            return user != null && user.getCurrentSessionId() != null
                    && !user.getCurrentSessionId().equals(tokenSessionId);
        }

        if (UserRole.VEHICLE.getValue().equalsIgnoreCase(userRole)) {
            Vehicle vehicle = vehicleRepository.findById(userId).orElse(null);
            return vehicle != null && vehicle.getCurrentSessionId() != null
                    && !vehicle.getCurrentSessionId().equals(tokenSessionId);
        }

        return false;
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
