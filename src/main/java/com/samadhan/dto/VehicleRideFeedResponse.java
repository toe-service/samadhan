package com.samadhan.dto;

import java.util.List;

import com.samadhan.entity.TransferRequestDetails;

// Response for the paginated /transfer/rideTransferByVehicle feed: a single page of rides for
// whichever status bucket was requested (PENDING/COMPLETED/OTHER), plus the pagination metadata
// needed to drive Prev/Next controls without re-fetching the whole list.
public class VehicleRideFeedResponse {

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
