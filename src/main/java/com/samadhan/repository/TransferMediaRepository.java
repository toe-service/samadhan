package com.samadhan.repository;

import com.samadhan.entity.TransferMedia;
import com.samadhan.enums.MediaUploadBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferMediaRepository extends JpaRepository<TransferMedia, Long> {
    List<TransferMedia> findByTransferRequestId(Long transferId);
    List<TransferMedia> findByTransferRequestIdAndMediaUploadBy(Long transferId, MediaUploadBy mediaUploadBy);
}
