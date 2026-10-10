package com.samadhan.dto;

import java.util.List;

import com.samadhan.entity.TransferRequestDetails;

// Paginated response for the Fleet/Team Performance page's ride-history drill-down (one
// vehicle/agent's full job list) -- same shape convention as MatchingRequestsResponse/
// RideFeedResponse: `rides` is just the current page (latest-first), while
// totalElements/totalPages describe the full, unpaginated history.
public class RideHistoryPageDto {

	private List<TransferRequestDetails> rides;
	private long totalElements;
	private int totalPages;
	private int page;
	private int size;

	public List<TransferRequestDetails> getRides() {
		return rides;
	}

	public void setRides(List<TransferRequestDetails> rides) {
		this.rides = rides;
	}

	public long getTotalElements() {
		return totalElements;
	}

	public void setTotalElements(long totalElements) {
		this.totalElements = totalElements;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
}
