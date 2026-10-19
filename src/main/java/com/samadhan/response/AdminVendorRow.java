package com.samadhan.response;

public class AdminVendorRow {
    private Long vendorId;
    private String vendorName;
    private String vendorEmail;
    private Integer vendorStatusCode;
    private String vendorStatusLabel;
    private Boolean isIndividual;
    private Double walletBalance;
    private Long vehicleCount;
    private Long agentCount;
    private Long pendingRequestCount;

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public Integer getVendorStatusCode() {
        return vendorStatusCode;
    }

    public void setVendorStatusCode(Integer vendorStatusCode) {
        this.vendorStatusCode = vendorStatusCode;
    }

    public String getVendorStatusLabel() {
        return vendorStatusLabel;
    }

    public void setVendorStatusLabel(String vendorStatusLabel) {
        this.vendorStatusLabel = vendorStatusLabel;
    }

    public Boolean getIsIndividual() {
        return isIndividual;
    }

    public void setIsIndividual(Boolean isIndividual) {
        this.isIndividual = isIndividual;
    }

    public Double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(Double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public Long getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(Long vehicleCount) {
        this.vehicleCount = vehicleCount;
    }

    public Long getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(Long agentCount) {
        this.agentCount = agentCount;
    }

    public Long getPendingRequestCount() {
        return pendingRequestCount;
    }

    public void setPendingRequestCount(Long pendingRequestCount) {
        this.pendingRequestCount = pendingRequestCount;
    }
}
