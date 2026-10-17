package com.samadhan.controller;

import com.samadhan.dto.RideHistoryPageDto;
import com.samadhan.entity.Driver;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.UserRole;
import com.samadhan.response.AdminDashboardSummaryResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

// Read-only, platform-wide views for the single admin account (see LoginService#loginAdmin) —
// total/onboarded/pending vendors, request status breakdown, and per-vendor vehicle/agent
// drill-down. Every endpoint here is gated by requireAdmin() (JWT role check) rather than the
// requireOwnVendor-style ownership check used elsewhere (DriverController, VendorAvailability-
// Controller) — an admin isn't "their own vendor", they're allowed to see every vendor's data.
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

    @GetMapping("/vendors")
    public ResponseEntity<ResponseObject<AdminVendorListResponse>> getVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        requireAdmin(httpRequest);
        AdminVendorListResponse vendors = adminService.getVendors(page, size);
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
}
