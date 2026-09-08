package com.samadhan.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.VendorAvailability;
import com.samadhan.request.VendorAvailabilityRequest;
import com.samadhan.response.ResponseObject;
import com.samadhan.security.TokenApi;
import com.samadhan.service.VendorAvailabilityService;
import com.samadhan.util.ResponseUtil;

@RestController
@RequestMapping(value = "/vendor")
public class VendorAvailabilityController {

	@Autowired
	VendorAvailabilityService vendorAvailabilityService;

	@Autowired
	TokenApi tokenApi;

	private Long extractVendorId(HttpServletRequest httpRequest) {
		String authHeader = httpRequest.getHeader("Authorization");
		String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
		return jwt != null ? tokenApi.extractUserId(jwt) : null;
	}

	// A vendor/individual owner declares "I'll be near <toLocation> on <expectedDate>" ahead of
	// time. The JWT's own vendorId must match request.vendorId, same ownership check used
	// elsewhere in TransferVendorController, so one vendor's token can't post on another's behalf.
	@PostMapping(value = "/postAvailability")
	public ResponseEntity<ResponseObject<VendorAvailability>> postAvailability(
			@RequestBody VendorAvailabilityRequest request, HttpServletRequest httpRequest) {

		Long tokenVendorId = extractVendorId(httpRequest);
		if (tokenVendorId == null || !tokenVendorId.equals(request.vendorId)) {
			throw new AccessDeniedException("You are not authorized to post availability for this vendor");
		}

		VendorAvailability availability = vendorAvailabilityService.postAvailability(request);
		ResponseObject<VendorAvailability> success = ResponseUtil.populateResponseObject(
				availability, "success", null);
		return ResponseEntity.ok(success);
	}

	@GetMapping(value = "/availability/{vendorId}")
	public ResponseEntity<ResponseObject<List<VendorAvailability>>> getActiveAvailability(
			@PathVariable Long vendorId, HttpServletRequest httpRequest) {

		Long tokenVendorId = extractVendorId(httpRequest);
		if (tokenVendorId == null || !tokenVendorId.equals(vendorId)) {
			throw new AccessDeniedException("You are not authorized to view this vendor's availability postings");
		}

		List<VendorAvailability> active = vendorAvailabilityService.getActiveForVendor(vendorId);
		ResponseObject<List<VendorAvailability>> success = ResponseUtil.populateResponseObject(
				active, "success", null);
		return ResponseEntity.ok(success);
	}

	// Backhaul matches: PENDING requests whose pickup lands near one of this vendor's active
	// availability postings, on that posting's expected date. Purely additive — doesn't touch
	// or replace the vendor's normal incoming-requests feed.
	@GetMapping(value = "/availability/{vendorId}/matching-requests")
	public ResponseEntity<ResponseObject<List<TransferRequestDetails>>> getMatchingRequests(
			@PathVariable Long vendorId, HttpServletRequest httpRequest) {

		Long tokenVendorId = extractVendorId(httpRequest);
		if (tokenVendorId == null || !tokenVendorId.equals(vendorId)) {
			throw new AccessDeniedException("You are not authorized to view this vendor's matching requests");
		}

		List<TransferRequestDetails> matches = vendorAvailabilityService.getRequestsMatchingAvailability(vendorId);
		ResponseObject<List<TransferRequestDetails>> success = ResponseUtil.populateResponseObject(
				matches, "success", null);
		return ResponseEntity.ok(success);
	}

	@DeleteMapping(value = "/availability/{vendorId}/{availabilityId}")
	public ResponseEntity<ResponseObject<String>> cancelAvailability(
			@PathVariable Long vendorId, @PathVariable Long availabilityId, HttpServletRequest httpRequest) {

		Long tokenVendorId = extractVendorId(httpRequest);
		if (tokenVendorId == null || !tokenVendorId.equals(vendorId)) {
			throw new AccessDeniedException("You are not authorized to cancel this vendor's availability posting");
		}

		vendorAvailabilityService.cancelAvailability(vendorId, availabilityId);
		ResponseObject<String> success = ResponseUtil.populateResponseObject(
				"Availability posting cancelled.", "success", null);
		return ResponseEntity.ok(success);
	}
}
