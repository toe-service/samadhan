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
}
