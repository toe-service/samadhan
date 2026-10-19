package com.samadhan.response;

// The owning vendor of a vehicle-number search hit (AdminVehicleSearchResult) — just enough to
// resolve both halves of an admin assignment (vendor + vehicle) from a single search result,
// same fields the admin vendor list/detail views already expose.
public class AdminVehicleVendorRef {
    private Long vendorId;
    private String vendorName;
    private Boolean isIndividual;

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

    public Boolean getIsIndividual() {
        return isIndividual;
    }

    public void setIsIndividual(Boolean isIndividual) {
        this.isIndividual = isIndividual;
    }
}
