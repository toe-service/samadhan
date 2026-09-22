package com.samadhan.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.samadhan.entity.Subscription;
import com.samadhan.entity.TransferVendor;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.repository.PaymentRepository;
import com.samadhan.repository.TransferVendorRepository;

@Component
@EnableScheduling
public class SubscriptionScheduler {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    @Autowired
    private PaymentRepository subscriptionRepository;

    @Autowired
    private TransferVendorRepository vendorRepository;

    @Scheduled(cron = "0 5 0 * * ?", zone = "Asia/Kolkata")		// Every day at 12:05 AM IST
    @Transactional
	public void suspendExpiredVendors() {

		// Pinned to IST, not the JVM default zone — endDate is a calendar date with no time-of-day,
		// so which zone "today" is computed in decides which side of midnight a vendor's expiry
		// falls on; a UTC-default server would flip vendors up to ~5.5 hours early/late otherwise.
		LocalDate today = LocalDate.now(IST);

		List<Subscription> expiredSubscriptions = subscriptionRepository.findByEndDateBeforeAndStatus(today);

		for (Subscription subscription : expiredSubscriptions) {

			TransferVendor vendor = subscription.getVendor();

			if (vendor != null) {
				vendor.setVendorStatus(VendorStatusEnum.SUSPENDED);
				vendorRepository.save(vendor);
			}

		}
	}
}
