package com.samadhan.repository;

// Projection for TransferVendorRepository#countVendorsByStatus — column aliases in that native
// query bind to these getters by name. vendor_status values per VendorStatusEnum: 0 =
// VERIFICATION_PENDING, 1 = Free_SUBSCRIPTION, 2 = SUBSCRIPTION_PENDING, 3 = ACTIVE, 4 = REJECTED,
// 5 = SUSPENDED. "Onboarded" = Free_SUBSCRIPTION or ACTIVE (same set TransferVendor#isSubscriber
// treats as subscribed); "pending" = VERIFICATION_PENDING or SUBSCRIPTION_PENDING.
public interface AdminVendorStatusCounts {
	Long getTotalVendors();
	Long getOnboardedVendors();
	Long getPendingVendors();
	Long getRejectedVendors();
	Long getSuspendedVendors();
}
