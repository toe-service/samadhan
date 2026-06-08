package com.samadhan.service;

import com.samadhan.entity.TransferMedia;
import com.samadhan.enums.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TransferMediaService {
    TransferMedia uploadMedia(Long transferId, MultipartFile file, MediaType mediaType);
    Map<String, List<Map<String, Object>>> getTransferMedia(Long transferId);
}
