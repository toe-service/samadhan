package com.samadhan.response;

public class AdminDashboardSummaryResponse {
    private long totalVendors;
    private long onboardedVendors;
    private long pendingVendors;
    private long rejectedVendors;
    private long suspendedVendors;

    private long totalRequests;
    private long pendingRequests;
    private long acceptedRequests;
    private long ongoingRequests;
    private long completedRequests;
    private long cancelledRequests;

    // Gross value of every COMPLETED ride (what customers paid) — see
    // TransferRequestRepository#getTotalEarning.
    private double totalEarning;

    // TransferEaze's own per-ride cut — Ride Acceptance/Start/Completion fees, LEAD FEE, and
    // registration fees. Deliberately excludes subscription revenue (see subscriptionEarning
    // below, surfaced separately) and isn't the gross ride value above. See
    // WalletTransactionRepo#getTotalCommissionEarning for exactly which transaction
    // types/descriptions count.
    private double commissionEarning;

    // Subscription purchase/renewal revenue, kept apart from commissionEarning — shown in the
    // Vendors section rather than folded into the per-ride commission figure.
    private double subscriptionEarning;

    public long getTotalVendors() {
        return totalVendors;
    }

    public void setTotalVendors(long totalVendors) {
        this.totalVendors = totalVendors;
    }

    public long getOnboardedVendors() {
        return onboardedVendors;
    }

    public void setOnboardedVendors(long onboardedVendors) {
        this.onboardedVendors = onboardedVendors;
    }

    public long getPendingVendors() {
        return pendingVendors;
    }

    public void setPendingVendors(long pendingVendors) {
        this.pendingVendors = pendingVendors;
    }

    public long getRejectedVendors() {
        return rejectedVendors;
    }

    public void setRejectedVendors(long rejectedVendors) {
        this.rejectedVendors = rejectedVendors;
    }

    public long getSuspendedVendors() {
        return suspendedVendors;
    }

    public void setSuspendedVendors(long suspendedVendors) {
        this.suspendedVendors = suspendedVendors;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getAcceptedRequests() {
        return acceptedRequests;
    }

    public void setAcceptedRequests(long acceptedRequests) {
        this.acceptedRequests = acceptedRequests;
    }

    public long getOngoingRequests() {
        return ongoingRequests;
    }

    public void setOngoingRequests(long ongoingRequests) {
        this.ongoingRequests = ongoingRequests;
    }

    public long getCompletedRequests() {
        return completedRequests;
    }

    public void setCompletedRequests(long completedRequests) {
        this.completedRequests = completedRequests;
    }

    public long getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(long cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public double getTotalEarning() {
        return totalEarning;
    }

    public void setTotalEarning(double totalEarning) {
        this.totalEarning = totalEarning;
    }

    public double getCommissionEarning() {
        return commissionEarning;
    }

    public void setCommissionEarning(double commissionEarning) {
        this.commissionEarning = commissionEarning;
    }

    public double getSubscriptionEarning() {
        return subscriptionEarning;
    }

    public void setSubscriptionEarning(double subscriptionEarning) {
        this.subscriptionEarning = subscriptionEarning;
    }
}
