package com.samadhan.controller;

import com.samadhan.dto.RideHistoryPageDto;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Subscription;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.entity.VendorWallet;
import com.samadhan.enums.UserRole;
import com.samadhan.request.AdminAssignRequest;
import com.samadhan.response.AdminDashboardSummaryResponse;
import com.samadhan.response.AdminVehicleSearchResult;
import com.samadhan.response.AdminVendorListResponse;
import com.samadhan.response.ResponseObject;
import com.samadhan.security.TokenApi;
import com.samadhan.service.AdminService;
import com.samadhan.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

// Platform-wide views (and, as of /requests/{id}/assign, one write action) for the single admin
// account (see LoginService#loginAdmin) — total/onboarded/pending vendors, request status
// breakdown, per-vendor vehicle/agent/wallet/subscription drill-down, and dispatching a request
// to a vendor+vehicle on their behalf. Every endpoint here is gated by requireAdmin() (JWT role
// check) rather than the requireOwnVendor-style ownership check used elsewhere (DriverController,
// VendorAvailabilityController) — an admin isn't "their own vendor", they're allowed to see
// (and act on) every vendor's data.
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TokenApi tokenApi;

    private void requireAdmin(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
        String role = jwt != null ? tokenApi.extractUserRole(jwt) : null;
        if (role == null || !UserRole.ADMIN.getValue().equalsIgnoreCase(role)) {
            throw new AccessDeniedException("You are not authorized to view the admin panel");
        }
    }

    @GetMapping("/dashboard-summary")
    public ResponseEntity<ResponseObject<AdminDashboardSummaryResponse>> getDashboardSummary(
            HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        AdminDashboardSummaryResponse summary = adminService.getDashboardSummary();
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(summary, "SUCCESS", null));
    }

    // search is optional — blank/absent returns the plain paginated list; with it, matches
    // vendor_name (case-insensitive, substring). Same endpoint backs both the Vendors section's
    // listing and the Assign modal's "By Vendor" lookup.
    @GetMapping("/vendors")
    public ResponseEntity<ResponseObject<AdminVendorListResponse>> getVendors(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        AdminVendorListResponse vendors = adminService.getVendors(search, page, size);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(vendors, "SUCCESS", null));
    }

    @GetMapping("/vendors/{vendorId}/vehicles")
    public ResponseEntity<ResponseObject<List<Vehicle>>> getVendorVehicles(
            @PathVariable Long vendorId, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        List<Vehicle> vehicles = adminService.getVendorVehicles(vendorId);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(vehicles, "SUCCESS", null));
    }

    @GetMapping("/vendors/{vendorId}/agents")
    public ResponseEntity<ResponseObject<List<Driver>>> getVendorAgents(
            @PathVariable Long vendorId, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        List<Driver> agents = adminService.getVendorAgents(vendorId);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(agents, "SUCCESS", null));
    }

    // { balance, securityDeposit } — same shape as the vendor-facing GET
    // /transferVendor/wallet-vendor/{vendorId} (SubscriptionManagement.jsx), just reachable with
    // an admin token instead of that vendor's own. response is omitted (not null) in the JSON
    // envelope if this vendor has no vendor_wallet row yet.
    @GetMapping("/vendors/{vendorId}/wallet")
    public ResponseEntity<ResponseObject<VendorWallet>> getVendorWallet(
            @PathVariable Long vendorId, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        VendorWallet wallet = adminService.getVendorWallet(vendorId);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(wallet, "SUCCESS", null));
    }

    // Same lookup as the vendor-facing GET /pay/subscriptions/{vendorId}. response is omitted
    // (not null) in the JSON envelope when the vendor has never subscribed.
    @GetMapping("/vendors/{vendorId}/subscription")
    public ResponseEntity<ResponseObject<Subscription>> getVendorSubscription(
            @PathVariable Long vendorId, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        Subscription subscription = adminService.getVendorSubscription(vendorId);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(subscription, "SUCCESS", null));
    }

    // Platform-wide request list with full details (vendor, customer, vehicle/agent, addresses,
    // cost) — not just the dashboard-summary counts. status is one of ALL/PENDING/ACCEPTED/
    // ONGOING/COMPLETED/CANCELLED (defaults to ALL), same bucket set the vendor ride feed uses.
    @GetMapping("/requests")
    public ResponseEntity<ResponseObject<RideHistoryPageDto>> getRequests(
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        RideHistoryPageDto requests = adminService.getRequests(status, page, size);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(requests, "SUCCESS", null));
    }

    // The Assign modal's "By Vehicle No." mode — a flat index across every vendor's fleet.
    // Returns an empty list for a blank query rather than the whole fleet table.
    @GetMapping("/vehicles/search")
    public ResponseEntity<ResponseObject<List<AdminVehicleSearchResult>>> searchVehicles(
            @RequestParam(required = false) String q, HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        List<AdminVehicleSearchResult> results = adminService.searchVehicles(q);
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(results, "SUCCESS", null));
    }

    // Dispatches a request to the given vendor's vehicle on the admin's say-so — delegates to
    // TransferRequestService#requestTransferApproval (the same path a vendor's own "Accept"
    // click uses), so it gets the same validation (vehicle must belong to vendorId, must be
    // active, wallet can't go below -200) and the same real wallet deduction (the per-ride
    // acceptance fee) that a normal accept charges, instead of a shortcut that skips all of that.
    @PostMapping("/requests/{requestId}/assign")
    public ResponseEntity<ResponseObject<TransferRequestDetails>> assignRequest(
            @PathVariable Long requestId,
            @RequestBody AdminAssignRequest body,
            HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        TransferRequestDetails updated =
                adminService.assignRequest(requestId, body.getVendorId(), body.getVehicleId());
        return ResponseEntity.ok(ResponseUtil.populateResponseObject(updated, "SUCCESS", null));
    }
}
