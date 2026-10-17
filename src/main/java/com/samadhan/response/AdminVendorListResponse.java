package com.samadhan.response;

import java.util.List;

public class AdminVendorListResponse {
    private List<AdminVendorRow> vendors;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public List<AdminVendorRow> getVendors() {
        return vendors;
    }

    public void setVendors(List<AdminVendorRow> vendors) {
        this.vendors = vendors;
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
}
