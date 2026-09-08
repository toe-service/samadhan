package com.samadhan.dto;

import java.util.List;

import com.samadhan.entity.TransferRequestDetails;

// Response for the paginated /transfer/showRidestoVendors feed: only `rides` (the current
// page, latest-first) is paginated — the four counts always reflect every ride visible to the
// vendor regardless of which page/status filter was requested, so the dashboard's summary
// cards (Total/Pending/Accepted/Ongoing) stay accurate while the table underneath pages.
public class RideFeedResponse {

	private List<TransferRequestDetails> rides;
	private long totalElements;
	private int totalPages;
	private int page;
	private int size;

	private long totalCount;
	private long pendingCount;
	private long acceptedCount;
	private long ongoingCount;

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

	public long getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long totalCount) {
		this.totalCount = totalCount;
	}

	public long getPendingCount() {
		return pendingCount;
	}

	public void setPendingCount(long pendingCount) {
		this.pendingCount = pendingCount;
	}

	public long getAcceptedCount() {
		return acceptedCount;
	}

	public void setAcceptedCount(long acceptedCount) {
		this.acceptedCount = acceptedCount;
	}

	public long getOngoingCount() {
		return ongoingCount;
	}

	public void setOngoingCount(long ongoingCount) {
		this.ongoingCount = ongoingCount;
	}
}
