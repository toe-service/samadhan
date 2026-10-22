package com.samadhan.service;

import com.samadhan.dto.RideHistoryPageDto;
import com.samadhan.entity.AbandonedFormSession;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Subscription;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorWallet;
import com.samadhan.enums.VendorStatusEnum;
import com.samadhan.repository.AbandonedFormSessionRepository;
import com.samadhan.repository.AdminRideStatusCounts;
import com.samadhan.repository.AdminVehicleSearchRow;
import com.samadhan.repository.AdminVendorStatusCounts;
import com.samadhan.repository.AdminVendorSummary;
import com.samadhan.repository.PaymentRepository;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VehicleRepository;
import com.samadhan.repository.VendorWalletRepository;
import com.samadhan.response.AdminAbandonedFormListResponse;
import com.samadhan.response.AdminDashboardSummaryResponse;
import com.samadhan.response.AdminVehicleSearchResult;
import com.samadhan.response.AdminVehicleVendorRef;
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

    @Autowired
    private VendorWalletRepository vendorWalletRepository;

    // Despite the name, this is the Subscription repository (JpaRepository<Subscription, Long>)
    // — see PaymentController#getSubscriptionsByVendor, the same vendor-facing endpoint this
    // reuses the lookup from.
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    // Assignment itself (POST /admin/requests/{id}/assign) delegates to the exact same accept
    // path a vendor's own dashboard uses (requestTransferApproval, transferApproval=1) rather
    // than reimplementing vendor/vehicle validation and the wallet-deduction fee here — see
    // assignRequest below.
    @Autowired
    private TransferRequestService transferRequestService;

    @Autowired
    private AbandonedFormSessionRepository abandonedFormSessionRepository;

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
    public AdminVendorListResponse getVendors(String search, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        String normalizedSearch = (search == null) ? "" : search.trim();
        Page<AdminVendorSummary> pageResult =
                transferVendorRepository.findAllVendorSummaries(normalizedSearch, pageable);

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
    public AdminAbandonedFormListResponse getAbandonedFormSessions(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        Page<AbandonedFormSession> pageResult = abandonedFormSessionRepository.findByResolvedFalseOrderByLastUpdatedAtDesc(pageable);

        AdminAbandonedFormListResponse response = new AdminAbandonedFormListResponse();
        response.setSessions(pageResult.getContent());
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

    @Override
    public VendorWallet getVendorWallet(Long vendorId) {
        return vendorWalletRepository.findByVendor(vendorId);
    }

    @Override
    public Subscription getVendorSubscription(Long vendorId) {
        return paymentRepository.findByVendorId(vendorId);
    }

    @Override
    public List<AdminVehicleSearchResult> searchVehicles(String query) {
        String normalized = (query == null) ? "" : query.trim();
        if (normalized.isEmpty()) {
            return List.of();
        }
        return vehicleRepository.searchForAdmin(normalized).stream()
                .map(this::toVehicleSearchResult)
                .collect(Collectors.toList());
    }

    @Override
    public TransferRequestDetails assignRequest(Long requestId, Long vendorId, Long vehicleId) {
        if (requestId == null || vendorId == null || vehicleId == null) {
            throw new IllegalArgumentException("requestId, vendorId, and vehicleId are all required");
        }
        return transferRequestService.requestTransferApproval(
                requestId, 1, vendorId, null, null, null, vehicleId.intValue(), "Vendor");
    }

    @Override
    public RideHistoryPageDto getRequests(String statusFilter, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.max(size, 1);
        String normalizedStatus = (statusFilter == null || statusFilter.isBlank())
                ? "ALL" : statusFilter.trim().toUpperCase();

        Page<TransferRequestDetails> pageResult = transferRequestRepository.findAllForAdmin(
                normalizedStatus, PageRequest.of(safePage, safeSize));

        RideHistoryPageDto dto = new RideHistoryPageDto();
        dto.setRides(pageResult.getContent());
        dto.setTotalElements(pageResult.getTotalElements());
        dto.setTotalPages(pageResult.getTotalPages());
        dto.setPage(safePage);
        dto.setSize(safeSize);
        return dto;
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
        row.setIsIndividual(summary.getIsIndividual());
        row.setWalletBalance(summary.getWalletBalance());
        row.setVehicleCount(orZero(summary.getVehicleCount()));
        row.setAgentCount(orZero(summary.getAgentCount()));
        row.setPendingRequestCount(orZero(summary.getPendingRequestCount()));
        return row;
    }

    private AdminVehicleSearchResult toVehicleSearchResult(AdminVehicleSearchRow row) {
        AdminVehicleSearchResult result = new AdminVehicleSearchResult();
        result.setId(row.getId());
        result.setVehicleNumber(row.getVehicleNumber());
        result.setVehicleContactNumber(row.getVehicleContactNumber());
        result.setIsActive(row.getIsActive());

        AdminVehicleVendorRef vendor = new AdminVehicleVendorRef();
        vendor.setVendorId(row.getVendorId());
        vendor.setVendorName(row.getVendorName());
        vendor.setIsIndividual(row.getIsIndividual());
        result.setVendor(vendor);

        return result;
    }

    private long orZero(Long value) {
        return value != null ? value : 0L;
    }
}
