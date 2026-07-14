package com.samadhan.scheduler;

import java.time.LocalDate;
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

    @Autowired
    private PaymentRepository subscriptionRepository;

    @Autowired
    private TransferVendorRepository vendorRepository;

    @Scheduled(cron = "0 5 0 * * ?")		// Every day at 12:05 AM
    @Transactional
	public void suspendExpiredVendors() {

		LocalDate today = LocalDate.now();

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
