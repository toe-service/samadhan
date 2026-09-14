package com.samadhan.dto;

import java.util.List;

import com.samadhan.entity.TransferRequestDetails;

// Paginated response for GET /vendor/availability/{vendorId}/matching-requests — same shape
// convention as RideFeedResponse: `matches` is just the current page (latest-first), while
// totalElements/totalPages describe the full, unpaginated match set.
public class MatchingRequestsResponse {

	private List<TransferRequestDetails> matches;
	private long totalElements;
	private int totalPages;
	private int page;
	private int size;

	public List<TransferRequestDetails> getMatches() {
		return matches;
	}

	public void setMatches(List<TransferRequestDetails> matches) {
		this.matches = matches;
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
