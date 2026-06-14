package com.samadhan.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class CachedTransferMedia {

    private final Map<String, List<Map<String, Object>>> data;
    private final long cachedAtMillis;

    public CachedTransferMedia(Map<String, List<Map<String, Object>>> data) {
        this.data = data;
        this.cachedAtMillis = Instant.now().toEpochMilli();
    }

    public Map<String, List<Map<String, Object>>> data() {
        return data;
    }

    public boolean isExpired(long cacheTtlMillis) {
        return Instant.now().toEpochMilli() - cachedAtMillis > cacheTtlMillis;
    }
}
