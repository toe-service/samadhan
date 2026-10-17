package com.samadhan.service;

import com.samadhan.entity.Driver;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.repository.AdminRideStatusCounts;
import com.samadhan.repository.AdminVendorStatusCounts;
import com.samadhan.repository.AdminVendorSummary;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.response.AdminDashboardSummaryResponse;
import com.samadhan.response.AdminVendorListResponse;
import com.samadhan.response.AdminVendorRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private TransferVendorRepository transferVendorRepository;

    @Autowired
    private TransferRequestRepository transferRequestRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private driversService driversService;

    @Override
    public AdminDashboardSummaryResponse getDashboardSummary() {
        AdminVendorStatusCounts vendorCounts = transferVendorRepository.countVendorsByStatus();
        AdminRideStatusCounts rideCounts = transferRequestRepository.countAllRidesByStatus();

        AdminDashboardSummaryResponse response = new AdminDashboardSummaryResponse();
        response.setTotalVendors(orZero(vendorCounts != null ? vendorCounts.getTotalVendors() : null));
        response.setOnboardedVendors(orZero(vendorCounts != null ? vendorCounts.getOnboardedVendors() : null));
        response.setPendingVendors(orZero(vendorCounts != null ? vendorCounts.getPendingVendors() : null));
        response.setRejectedVendors(orZero(vendorCounts != null ? vendorCounts.getRejectedVendors() : null));
        response.setSuspendedVendors(orZero(vendorCounts != null ? vendorCounts.getSuspendedVendors() : null));

        response.setTotalRequests(orZero(rideCounts != null ? rideCounts.getTotal() : null));
        response.setPendingRequests(orZero(rideCounts != null ? rideCounts.getPending() : null));
        response.setAcceptedRequests(orZero(rideCounts != null ? rideCounts.getAccepted() : null));
        response.setOngoingRequests(orZero(rideCounts != null ? rideCounts.getOngoing() : null));
        response.setCompletedRequests(orZero(rideCounts != null ? rideCounts.getCompleted() : null));
        response.setCancelledRequests(orZero(rideCounts != null ? rideCounts.getCancelled() : null));
        return response;
    }

    @Override
    public AdminVendorListResponse getVendors(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<AdminVendorSummary> pageResult = transferVendorRepository.findAllVendorSummaries(pageable);

        List<AdminVendorRow> rows = pageResult.getContent().stream().map(this::toRow).collect(Collectors.toList());

        AdminVendorListResponse response = new AdminVendorListResponse();
        response.setVendors(rows);
        response.setPage(safePage);
        response.setSize(safeSize);
        response.setTotalElements(pageResult.getTotalElements());
        response.setTotalPages(pageResult.getTotalPages());
        return response;
    }

    @Override
    public List<Vehicle> getVendorVehicles(Long vendorId) {
        return vehicleService.getAllVehiclesByVendor(vendorId, false);
    }

    @Override
    public List<Driver> getVendorAgents(Long vendorId) {
        return driversService.getAllDriversByVendor(vendorId);
    }

    private AdminVendorRow toRow(AdminVendorSummary summary) {
        AdminVendorRow row = new AdminVendorRow();
        row.setVendorId(summary.getVendorId());
        row.setVendorName(summary.getVendorName());
        row.setVendorEmail(summary.getVendorEmail());
        row.setVendorStatusCode(summary.getVendorStatus());
        row.setVendorStatusLabel(summary.getVendorStatus() != null
                ? VendorStatusEnum.getTypeById(summary.getVendorStatus())
                : null);
        row.setVehicleCount(orZero(summary.getVehicleCount()));
        row.setAgentCount(orZero(summary.getAgentCount()));
        row.setPendingRequestCount(orZero(summary.getPendingRequestCount()));
        return row;
    }

    private long orZero(Long value) {
        return value != null ? value : 0L;
    }
}
