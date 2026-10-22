package com.samadhan.response;

import com.samadhan.entity.AbandonedFormSession;

import java.util.List;

public class AdminAbandonedFormListResponse {
    private List<AbandonedFormSession> sessions;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public List<AbandonedFormSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<AbandonedFormSession> sessions) {
        this.sessions = sessions;
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
