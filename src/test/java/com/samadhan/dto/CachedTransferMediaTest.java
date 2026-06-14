package com.samadhan.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class CachedTransferMediaTest {

    @Test
    void testDataRetrieval() {
        Map<String, List<Map<String, Object>>> data = new HashMap<>();
        data.put("images", Collections.emptyList());
        CachedTransferMedia cached = new CachedTransferMedia(data);

        Assertions.assertEquals(data, cached.data());
    }

    @Test
    void testIsExpired() throws InterruptedException {
        Map<String, List<Map<String, Object>>> data = Collections.emptyMap();
        CachedTransferMedia cached = new CachedTransferMedia(data);

        // Not expired yet
        Assertions.assertFalse(cached.isExpired(1000));

        // Wait to expire
        Thread.sleep(100);
        Assertions.assertTrue(cached.isExpired(50));
    }
}
