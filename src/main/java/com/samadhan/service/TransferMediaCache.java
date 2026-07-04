package com.samadhan.service;

import com.samadhan.dto.CachedTransferMedia;
import com.samadhan.enums.MediaUploadBy;
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

    private final Map<String, CachedTransferMedia> cache = new ConcurrentHashMap<>();

    private String getCacheKey(Long transferId, MediaUploadBy mediaUploadBy) {
        return transferId + ":" + mediaUploadBy;
    }

    public Map<String, List<Map<String, Object>>> get(Long transferId, MediaUploadBy mediaUploadBy) {
        String key = getCacheKey(transferId, mediaUploadBy);
        CachedTransferMedia cachedTransferMedia = cache.get(key);
        if (cachedTransferMedia != null) {
            if (!cachedTransferMedia.isExpired(cacheTtlMillis)) {
                logger.info("Cache hit for transferId: {}, uploadBy: {}", transferId, mediaUploadBy);
                return cachedTransferMedia.data();
            } else {
                logger.info("Cache expired for transferId: {}, uploadBy: {}", transferId, mediaUploadBy);
            }
        } else {
            logger.info("Cache miss for transferId: {}, uploadBy: {}", transferId, mediaUploadBy);
        }
        return null;
    }

    public void put(Long transferId, MediaUploadBy mediaUploadBy, Map<String, List<Map<String, Object>>> data) {
        logger.info("Updating cache for transferId: {}, uploadBy: {}", transferId, mediaUploadBy);
        cache.put(getCacheKey(transferId, mediaUploadBy), new CachedTransferMedia(data));
    }

    public void evict(Long transferId, MediaUploadBy mediaUploadBy) {
        logger.info("Evicting cache for transferId: {}, uploadBy: {}", transferId, mediaUploadBy);
        cache.remove(getCacheKey(transferId, mediaUploadBy));
    }
}
