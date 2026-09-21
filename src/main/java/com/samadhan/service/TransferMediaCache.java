package com.samadhan.service;

import com.samadhan.dto.CachedTransferMedia;
import com.samadhan.enums.MediaUploadBy;
import com.samadhan.enums.RideStage;
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

    private String getCacheKey(Long transferId, MediaUploadBy mediaUploadBy, RideStage rideStage) {
        return transferId + ":" + mediaUploadBy + ":" + rideStage;
    }

    public Map<String, List<Map<String, Object>>> get(Long transferId, MediaUploadBy mediaUploadBy, RideStage rideStage) {
        String key = getCacheKey(transferId, mediaUploadBy, rideStage);
        CachedTransferMedia cachedTransferMedia = cache.get(key);
        if (cachedTransferMedia != null) {
            if (!cachedTransferMedia.isExpired(cacheTtlMillis)) {
                logger.info("Cache hit for transferId: {}, uploadBy: {}, rideStage: {}", transferId, mediaUploadBy, rideStage);
                return cachedTransferMedia.data();
            } else {
                logger.info("Cache expired for transferId: {}, uploadBy: {}, rideStage: {}", transferId, mediaUploadBy, rideStage);
            }
        } else {
            logger.info("Cache miss for transferId: {}, uploadBy: {}, rideStage: {}", transferId, mediaUploadBy, rideStage);
        }
        return null;
    }

    public void put(Long transferId, MediaUploadBy mediaUploadBy, RideStage rideStage, Map<String, List<Map<String, Object>>> data) {
        logger.info("Updating cache for transferId: {}, uploadBy: {}, rideStage: {}", transferId, mediaUploadBy, rideStage);
        cache.put(getCacheKey(transferId, mediaUploadBy, rideStage), new CachedTransferMedia(data));
    }

    public void evict(Long transferId, MediaUploadBy mediaUploadBy, RideStage rideStage) {
        logger.info("Evicting cache for transferId: {}, uploadBy: {}, rideStage: {}", transferId, mediaUploadBy, rideStage);
        cache.remove(getCacheKey(transferId, mediaUploadBy, rideStage));
    }
}
