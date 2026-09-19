package com.samadhan.scheduler;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.samadhan.entity.UserDetails;
import com.samadhan.repository.AvailableRideSummaryProjection;
import com.samadhan.repository.UserRepository;
import com.samadhan.repository.VendorAvailabilityRepository;
import com.samadhan.util.FireBaseMessagingService;

// Once a day, tells users in a route's origin city that vendor capacity is available tomorrow
// ("N vehicles traveling City A -> City B tomorrow — shift your goods, get 10% off"). Matches
// UserDetails.deviceCity against VendorAvailability.fromCity (both best-effort — deviceCity is
// whatever the app last reported, fromCity is CityUtils.extractCity's heuristic parse). Capped at
// one push per user per day via last_availability_notified_date, even if a user's city matches
// more than one route on the same day — see UserRepository#findUsersToNotifyForCity.
@Component
public class AvailableRidesNotificationScheduler {

	private static final Logger log = LoggerFactory.getLogger(AvailableRidesNotificationScheduler.class);

	@Autowired
	private VendorAvailabilityRepository vendorAvailabilityRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private FireBaseMessagingService fireBaseMessagingService;

	@Scheduled(cron = "0 0 9 * * ?")		// Once daily, 9 AM
	public void notifyAvailableRidesForTomorrow() {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		List<AvailableRideSummaryProjection> tomorrowsRoutes =
				vendorAvailabilityRepository.findAvailableRideSummariesOnDate(tomorrow);

		for (AvailableRideSummaryProjection route : tomorrowsRoutes) {
			List<UserDetails> usersToNotify =
					userRepository.findUsersToNotifyForCity(route.getFromCity(), today);

			if (usersToNotify.isEmpty()) {
				continue;
			}

			String title = "Vehicles available tomorrow!";
			String body = String.format(
					"%d vehicle%s traveling %s → %s tomorrow. Shift your goods and get 10%% off!",
					route.getVehicleCount(), route.getVehicleCount() == 1 ? "" : "s",
					route.getFromCity(), route.getToCity());

			Map<String, String> data = new HashMap<>();
			data.put("type", "AVAILABLE_RIDES");
			data.put("fromCity", route.getFromCity());
			data.put("toCity", route.getToCity());

			for (UserDetails user : usersToNotify) {
				try {
					fireBaseMessagingService.sendPushNotification(user.getFcmToken(), title, body, data);
				} catch (Exception e) {
					// Best-effort — leave last_availability_notified_date unset so this user is
					// retried on tomorrow's run instead of being silently skipped over a transient
					// failure, same convention as OverduePickupScheduler.
					log.warn("Failed to send available-rides notification to user {}: {}",
							user.getId(), e.getMessage(), e);
					continue;
				}
				user.setLastAvailabilityNotifiedDate(today);
				userRepository.save(user);
			}
		}
	}
}
