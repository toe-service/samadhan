package com.samadhan.response;

// One row of GET /admin/vehicles/search?q= — lets the admin panel resolve a request straight to
// a specific vehicle (and its owning vendor) by plate number, without knowing the vendor's name
// first. See VehicleRepository#searchForAdmin for the backing query.
public class AdminVehicleSearchResult {
    private Long id;
    private String vehicleNumber;
    private String vehicleContactNumber;
    private Boolean isActive;
    private AdminVehicleVendorRef vendor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleContactNumber() {
        return vehicleContactNumber;
    }

    public void setVehicleContactNumber(String vehicleContactNumber) {
        this.vehicleContactNumber = vehicleContactNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public AdminVehicleVendorRef getVendor() {
        return vendor;
    }

    public void setVendor(AdminVehicleVendorRef vendor) {
        this.vendor = vendor;
    }
}
