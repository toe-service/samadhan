package com.samadhan.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.samadhan.service.VendorAvailabilityService;

// One-time seed for vendor_availability_match, the precomputed table backing "Matching Your
// Posting" (see VendorAvailabilityServiceImpl). Runs on every app startup but no-ops immediately
// once the table has any rows at all — only does real work the first time this feature is
// deployed, when the table starts out empty and would otherwise stay empty until vendors happen
// to edit an existing posting.
@Component
public class VendorAvailabilityMatchBootstrap implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(VendorAvailabilityMatchBootstrap.class);

	@Autowired
	private VendorAvailabilityService vendorAvailabilityService;

	@Override
	public void run(String... args) {
		try {
			vendorAvailabilityService.bootstrapMatchesIfEmpty();
		} catch (Exception e) {
			log.warn("Vendor availability match bootstrap failed: {}", e.getMessage(), e);
		}
	}
}
