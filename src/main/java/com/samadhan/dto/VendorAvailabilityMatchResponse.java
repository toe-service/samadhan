package com.samadhan.dto;

import com.samadhan.entity.TransferRequestDetails;

import lombok.Data;
import lombok.NoArgsConstructor;

// One PENDING ride matched to one of the vendor's active availability postings, tagged with why
// it matched: POSTING_ROUTE (near the posting's destination or along the from->to driving route)
// or RETURN_TRIP (near the posting's own starting point).
@Data
@NoArgsConstructor
public class VendorAvailabilityMatchResponse {
	private TransferRequestDetails request;
	private Long availabilityId;
	private String matchType;
	private double matchDistanceKm;
}
