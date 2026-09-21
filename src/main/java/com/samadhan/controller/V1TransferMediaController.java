package com.samadhan.controller;

import com.samadhan.dto.CachedTransferMedia;
import com.samadhan.entity.TransferMedia;
import com.samadhan.enums.MediaType;
import com.samadhan.enums.MediaUploadBy;
import com.samadhan.enums.RideStage;
import com.samadhan.service.TransferMediaCache;
import com.samadhan.service.TransferMediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
public class V1TransferMediaController {

    private final TransferMediaService transferMediaService;
    private final TransferMediaCache transferMediaCache;

    public V1TransferMediaController(TransferMediaService transferMediaService, TransferMediaCache transferMediaCache) {
        this.transferMediaService = transferMediaService;
        this.transferMediaCache = transferMediaCache;
    }

    @PostMapping("/{transferId}/media")
    public ResponseEntity<?> uploadMedia(
            @PathVariable Long transferId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") MediaType mediaType,
            @RequestParam("mediaUploadBy") MediaUploadBy mediaUploadBy,
            @RequestParam(value = "rideStage", required = false) RideStage rideStage) {

        if (mediaUploadBy == MediaUploadBy.VEHICLE && rideStage == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "rideStage is required when mediaUploadBy is VEHICLE"));
        }

        TransferMedia media = transferMediaService.uploadMedia(transferId, file, mediaType, mediaUploadBy, rideStage);

        transferMediaCache.evict(transferId, mediaUploadBy, rideStage);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mediaId", media.getId());
        response.put("message", "Uploaded Successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transferId}/media")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getTransferMedia(
            @PathVariable Long transferId,
            @RequestParam("mediaUploadBy") MediaUploadBy mediaUploadBy,
            @RequestParam(value = "rideStage", required = false) RideStage rideStage) {

        Map<String, List<Map<String, Object>>> cachedData = transferMediaCache.get(transferId, mediaUploadBy, rideStage);

        if (cachedData != null) {
            return ResponseEntity.ok(cachedData);
        }

        Map<String, List<Map<String, Object>>> transferMedia =
                transferMediaService.getTransferMedia(transferId, mediaUploadBy, rideStage);

        transferMediaCache.put(transferId, mediaUploadBy, rideStage, transferMedia);

        return ResponseEntity.ok(transferMedia);
    }
}
