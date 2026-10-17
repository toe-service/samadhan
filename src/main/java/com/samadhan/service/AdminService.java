package com.samadhan.service;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Vehicle;
import com.samadhan.response.AdminDashboardSummaryResponse;
import com.samadhan.response.AdminVendorListResponse;

import java.util.List;

public interface AdminService {
    AdminDashboardSummaryResponse getDashboardSummary();

    AdminVendorListResponse getVendors(int page, int size);

    List<Vehicle> getVendorVehicles(Long vendorId);

    List<Driver> getVendorAgents(Long vendorId);
}
