package com.samadhan.service;

import com.samadhan.entity.TransferMedia;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.enums.MediaType;
import com.samadhan.enums.MediaUploadBy;
import com.samadhan.exception.ResourceNotFoundException;
import com.samadhan.repository.TransferMediaRepository;
import com.samadhan.repository.TransferRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransferMediaServiceImpl implements TransferMediaService {

    private static final Logger log = LoggerFactory.getLogger(TransferMediaServiceImpl.class);

    private final TransferMediaRepository transferMediaRepository;
    private final TransferRequestRepository transferRequestRepository;
    private final StorageService storageService;

    public TransferMediaServiceImpl(TransferMediaRepository transferMediaRepository, TransferRequestRepository transferRequestRepository, StorageService storageService) {
        this.transferMediaRepository = transferMediaRepository;
        this.transferRequestRepository = transferRequestRepository;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public TransferMedia uploadMedia(Long transferId, MultipartFile file, MediaType mediaType, MediaUploadBy mediaUploadBy) {
        TransferRequestDetails transferRequest = transferRequestRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("TransferRequestDetails not found with id: " + transferId));

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        // Storage key: transfers/{transferId}/{timestamp}_{originalName}
        String storageKey = String.format("transfers/%d/%d_%s", transferId, System.currentTimeMillis(), originalFilename);

        try {
            storageService.uploadFile(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.error("Failed to upload file to storage, saving media record without file", e);
        }

        TransferMedia transferMedia = new TransferMedia();
        transferMedia.setTransferRequest(transferRequest);
        transferMedia.setMediaType(mediaType);
        transferMedia.setMediaUploadBy(mediaUploadBy);
        transferMedia.setOriginalFileName(originalFilename);
        transferMedia.setStorageKey(storageKey);
        transferMedia.setContentType(file.getContentType());
        transferMedia.setFileSize(file.getSize());

        return transferMediaRepository.save(transferMedia);
    }

    @Override
    public Map<String, List<Map<String, Object>>> getTransferMedia(Long transferId, MediaUploadBy mediaUploadBy) {
        List<TransferMedia> mediaList = transferMediaRepository.findByTransferRequestIdAndMediaUploadBy(transferId, mediaUploadBy);

        List<Map<String, Object>> photos = new ArrayList<>();
        List<Map<String, Object>> videos = new ArrayList<>();

        for (TransferMedia media : mediaList) {
            Map<String, Object> mediaMap = new HashMap<>();
            mediaMap.put("id", media.getId());
            mediaMap.put("url", storageService.generatePresignedUrl(media.getStorageKey()));

            if (media.getMediaType() == MediaType.PHOTO) {
                photos.add(mediaMap);
            } else if (media.getMediaType() == MediaType.VIDEO) {
                videos.add(mediaMap);
            }
        }

        Map<String, List<Map<String, Object>>> response = new HashMap<>();
        response.put("photos", photos);
        response.put("videos", videos);

        return response;
    }
}
