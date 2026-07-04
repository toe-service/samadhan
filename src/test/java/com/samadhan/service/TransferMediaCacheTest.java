package com.samadhan.service;

import com.samadhan.dto.CachedTransferMedia;
import com.samadhan.enums.MediaUploadBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class TransferMediaCacheTest {

    private TransferMediaCache transferMediaCache;
    private final long TTL = 1000;

    @BeforeEach
    void setUp() {
        transferMediaCache = new TransferMediaCache();
        ReflectionTestUtils.setField(transferMediaCache, "cacheTtlMillis", TTL);
    }

    @Test
    void testPutAndGet() {
        Long transferId = 1L;
        MediaUploadBy uploadBy = MediaUploadBy.AGENT;
        Map<String, List<Map<String, Object>>> data = Collections.emptyMap();

        transferMediaCache.put(transferId, uploadBy, data);
        Map<String, List<Map<String, Object>>> cachedData = transferMediaCache.get(transferId, uploadBy);

        Assertions.assertNotNull(cachedData);
        Assertions.assertEquals(data, cachedData);
    }

    @Test
    void testGetNonExistent() {
        Assertions.assertNull(transferMediaCache.get(999L, MediaUploadBy.AGENT));
    }

    @Test
    void testEvict() {
        Long transferId = 1L;
        MediaUploadBy uploadBy = MediaUploadBy.AGENT;
        transferMediaCache.put(transferId, uploadBy, Collections.emptyMap());
        Assertions.assertNotNull(transferMediaCache.get(transferId, uploadBy));

        transferMediaCache.evict(transferId, uploadBy);
        Assertions.assertNull(transferMediaCache.get(transferId, uploadBy));
    }

    @Test
    void testExpiration() throws InterruptedException {
        Long transferId = 1L;
        MediaUploadBy uploadBy = MediaUploadBy.AGENT;
        ReflectionTestUtils.setField(transferMediaCache, "cacheTtlMillis", 50L);
        transferMediaCache.put(transferId, uploadBy, Collections.emptyMap());

        Assertions.assertNotNull(transferMediaCache.get(transferId, uploadBy));

        Thread.sleep(100);

        Assertions.assertNull(transferMediaCache.get(transferId, uploadBy));
    }
}
