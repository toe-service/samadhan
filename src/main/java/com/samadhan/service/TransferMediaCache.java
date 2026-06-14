package com.samadhan.service;

import com.samadhan.dto.CachedTransferMedia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TransferMediaCache {

    private static final Logger logger = LoggerFactory.getLogger(TransferMediaCache.class);

    @Value("${transfer.cache.ttl.millis}")
    private long cacheTtlMillis;

    private final Map<Long, CachedTransferMedia> cache = new ConcurrentHashMap<>();

    public Map<String, List<Map<String, Object>>> get(Long transferId) {
        CachedTransferMedia cachedTransferMedia = cache.get(transferId);
        if (cachedTransferMedia != null) {
            if (!cachedTransferMedia.isExpired(cacheTtlMillis)) {
                logger.info("Cache hit for transferId: {}", transferId);
                return cachedTransferMedia.data();
            } else {
                logger.info("Cache expired for transferId: {}", transferId);
            }
        } else {
            logger.info("Cache miss for transferId: {}", transferId);
        }
        return null;
    }

    public void put(Long transferId, Map<String, List<Map<String, Object>>> data) {
        logger.info("Updating cache for transferId: {}", transferId);
        cache.put(transferId, new CachedTransferMedia(data));
    }

    public void evict(Long transferId) {
        logger.info("Evicting cache for transferId: {}", transferId);
        cache.remove(transferId);
    }
}
