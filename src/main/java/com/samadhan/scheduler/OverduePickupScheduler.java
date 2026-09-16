package com.samadhan.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.util.FireBaseMessagingService;

// Makes sure a PENDING, unassigned request never just silently sits unpicked past its pickup
// time. Two complementary checks, both re-running the same nearby-vehicle push notification used
// when the request was first created, so vendors get another chance to see it:
//   - notifyOverduePickups: reactive — fires once the pickup window has already fully passed.
//   - sendPrePickupReminders: proactive — fires PICKUP_REMINDER_LEAD_MINUTES before a scheduled
//     window opens (e.g. 2:30 PM for a "3 PM - 6 PM" slot), so there's a chance to get it accepted
//     before it becomes overdue in the first place.
// Each request is only notified once per check (tracked via overdue_notified_at /
// pickup_reminder_sent_at), not repeatedly on every run.
@Component
public class OverduePickupScheduler {

	private static final Logger log = LoggerFactory.getLogger(OverduePickupScheduler.class);

	// How long a still-pending instant/immediate booking is given before being flagged overdue —
	// these are meant to be picked up within minutes, not scheduled days out, so this is a much
	// tighter window than the day-level granularity used for scheduled bookings.
	private static final int INSTANT_BOOKING_OVERDUE_MINUTES = 30;

	// How far ahead of a scheduled pickup window's start to send the proactive reminder.
	private static final int PICKUP_REMINDER_LEAD_MINUTES = 30;

	// The only values pickup_schedule can actually hold for a non-instant booking — see the live
	// <select> in CreateTransfer.jsx/BookVehicle.jsx/HomeShifting.jsx on the frontend, all three of
	// which offer exactly these four slot labels with no custom/free-text option. Mapped here to
	// each slot's start time so "is it within the reminder window yet" can be computed precisely.
	// A pickup_schedule value that doesn't match any of these (e.g. a legacy/unrecognized value)
	// is simply skipped rather than guessed at.
	private static final Map<String, LocalTime> SCHEDULE_SLOT_START_TIMES = Map.of(
			"9 AM - 12 PM", LocalTime.of(9, 0),
			"12 PM - 3 PM", LocalTime.of(12, 0),
			"3 PM - 6 PM", LocalTime.of(15, 0),
			"6 PM - 9 PM", LocalTime.of(18, 0)
	);

	@Autowired
	private TransferRequestRepository transferRequestRepository;

	@Autowired
	private FireBaseMessagingService fireBaseMessagingService;

	@Scheduled(cron = "0 */15 * * * ?")		// Every 15 minutes
	public void notifyOverduePickups() {
		LocalDate today = LocalDate.now();
		LocalDateTime instantBookingCutoff = LocalDateTime.now().minusMinutes(INSTANT_BOOKING_OVERDUE_MINUTES);

		List<TransferRequestDetails> overdue =
				transferRequestRepository.findOverduePendingUnassigned(today, instantBookingCutoff);

		for (TransferRequestDetails request : overdue) {
			try {
				fireBaseMessagingService.notifyVehicles(request);
			} catch (Exception e) {
				// Best-effort — leave overdue_notified_at unset so this request is retried on the
				// next run instead of being silently skipped forever over a transient failure.
				log.warn("Failed to send overdue-pickup notification for request {}: {}",
						request.getId(), e.getMessage(), e);
				continue;
			}
			request.setOverdueNotifiedAt(LocalDateTime.now());
			transferRequestRepository.save(request);
		}
	}

	@Scheduled(cron = "0 */15 * * * ?")		// Every 15 minutes
	public void sendPrePickupReminders() {
		LocalDate today = LocalDate.now();
		LocalDateTime now = LocalDateTime.now();

		List<TransferRequestDetails> candidates =
				transferRequestRepository.findScheduledPendingUnassignedToday(today);

		for (TransferRequestDetails request : candidates) {
			LocalTime windowStart = SCHEDULE_SLOT_START_TIMES.get(request.getPickupSchedule());
			if (windowStart == null) {
				continue;
			}

			LocalDateTime windowStartDateTime = LocalDateTime.of(today, windowStart);
			LocalDateTime reminderTime = windowStartDateTime.minusMinutes(PICKUP_REMINDER_LEAD_MINUTES);

			// Not yet within the reminder lead time, or the window has already started (the
			// overdue check above takes over once it's actually passed) — nothing to do this run.
			if (now.isBefore(reminderTime) || !now.isBefore(windowStartDateTime)) {
				continue;
			}

			try {
				fireBaseMessagingService.notifyVehicles(request);
			} catch (Exception e) {
				log.warn("Failed to send pre-pickup reminder for request {}: {}",
						request.getId(), e.getMessage(), e);
				continue;
			}
			request.setPickupReminderSentAt(now);
			transferRequestRepository.save(request);
		}
	}
}
