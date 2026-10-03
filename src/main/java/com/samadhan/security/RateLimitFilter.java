package com.samadhan.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.samadhan.response.Error;
import com.samadhan.util.ResponseUtil;

// Global, per-IP rate limit — runs before JwtAuthenticationFilter (see SecurityConfig) so it
// covers every request Spring Security's chain sees, authenticated or not (public endpoints like
// requestRideTransfer/register-vendor/send-otp included), rather than only the ones that make it
// past auth.
//
// Fixed-window counter, not a token bucket: a client could in principle send CAPACITY requests at
// the very end of one window and CAPACITY more right at the start of the next, briefly seeing up
// to 2x CAPACITY in quick succession at that boundary. That's a known, standard trade-off for this
// algorithm's simplicity — acceptable for a general anti-abuse measure, not something this app is
// relying on for fine-grained precision. Picked over a token-bucket library specifically because
// it only needs Caffeine's well-established cache API (expireAfterWrite does the window-reset
// bookkeeping for free) rather than a second, less-familiar dependency.
@Component
public class RateLimitFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

	private static final int CAPACITY = 60;
	private static final Duration WINDOW = Duration.ofSeconds(30);

	// Railway's own health check — rate-limiting this risks the platform mistaking a throttled
	// health check for an unhealthy instance and restarting/rerouting it, a self-inflicted outage
	// far worse than whatever this filter is meant to prevent.
	private static final Set<String> EXCLUDED_PATHS = Set.of("/health");

	@Autowired
	private ObjectMapper objectMapper;

	// expireAfterWrite means each IP's counter resets WINDOW after its first request in that
	// window, not on a wall-clock boundary shared across IPs — and doubles as eviction, so an IP
	// that stops making requests doesn't keep its entry (and the map's memory) forever.
	private final Cache<String, AtomicInteger> requestCounts = Caffeine.newBuilder()
			.expireAfterWrite(WINDOW)
			.maximumSize(100_000)
			.build();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		if (EXCLUDED_PATHS.contains(request.getRequestURI())) {
			filterChain.doFilter(request, response);
			return;
		}

		String clientIp = resolveClientIp(request);
		AtomicInteger count = requestCounts.get(clientIp, ip -> new AtomicInteger(0));

		if (count.incrementAndGet() > CAPACITY) {
			log.warn("Rate limit exceeded for {} on {} {}", clientIp, request.getMethod(), request.getRequestURI());
			response.setStatus(429);
			response.setContentType("application/json");
			response.getWriter().write(objectMapper.writeValueAsString(
					ResponseUtil.populateResponseObject(null, "429",
							new Error("RateLimit", "Too many requests. Please slow down and try again shortly."))));
			return;
		}

		filterChain.doFilter(request, response);
	}

	// Railway (like most PaaS/proxy setups) terminates the real client connection at an edge
	// proxy and forwards to this app over an internal network — request.getRemoteAddr() would see
	// only that proxy's own IP on every request, making every caller look identical. The first
	// entry in X-Forwarded-For is the original client; trusted here because this app is only ever
	// reached through Railway's proxy, not exposed directly on some other path a client could use
	// to forge this header straight to the app.
	private String resolveClientIp(HttpServletRequest request) {
		String forwardedFor = request.getHeader("X-Forwarded-For");
		if (forwardedFor != null && !forwardedFor.isBlank()) {
			return forwardedFor.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
}
