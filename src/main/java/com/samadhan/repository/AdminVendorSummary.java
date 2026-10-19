package com.samadhan.repository;

// Projection for TransferVendorRepository#findAllVendorSummaries — one row per vendor for the
// admin panel's vendor list. vendorStatus is the raw VendorStatusEnum ordinal (see
// AdminVendorStatusCounts) — resolved to a label on the frontend/response layer, not here.
public interface AdminVendorSummary {
	Long getVendorId();
	String getVendorName();
	String getVendorEmail();
	Integer getVendorStatus();
	Boolean getIsIndividual();
	Double getWalletBalance();
	Long getVehicleCount();
	Long getAgentCount();
	Long getPendingRequestCount();
}
