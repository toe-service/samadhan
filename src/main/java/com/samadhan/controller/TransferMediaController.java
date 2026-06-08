package com.samadhan.controller;

import com.samadhan.entity.TransferMedia;
import com.samadhan.enums.MediaType;
import com.samadhan.service.TransferMediaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transfers")
public class TransferMediaController {

    private final TransferMediaService transferMediaService;

    public TransferMediaController(TransferMediaService transferMediaService) {
        this.transferMediaService = transferMediaService;
    }

    @PostMapping("/{transferId}/media")
    public ResponseEntity<?> uploadMedia(
            @PathVariable Long transferId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("mediaType") MediaType mediaType) {
        
        TransferMedia media = transferMediaService.uploadMedia(transferId, file, mediaType);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mediaId", media.getId());
        response.put("message", "Uploaded Successfully");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{transferId}/media")
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getTransferMedia(
            @PathVariable Long transferId) {
        
        return ResponseEntity.ok(transferMediaService.getTransferMedia(transferId));
    }
}
