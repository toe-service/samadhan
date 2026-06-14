package com.samadhan.controller;

import com.samadhan.entity.TransferMedia;
import com.samadhan.enums.MediaType;
import com.samadhan.service.TransferMediaCache;
import com.samadhan.service.TransferMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class V1TransferMediaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransferMediaService transferMediaService;

    @Mock
    private TransferMediaCache transferMediaCache;

    @InjectMocks
    private V1TransferMediaController v1TransferMediaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(v1TransferMediaController).build();
    }

    @Test
    void testUploadMedia() throws Exception {
        Long transferId = 1L;
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test data".getBytes());
        TransferMedia media = new TransferMedia();
        media.setId(100L);

        when(transferMediaService.uploadMedia(eq(transferId), any(), eq(MediaType.PHOTO))).thenReturn(media);

        mockMvc.perform(multipart("/api/transfers/" + transferId + "/media")
                .file(file)
                .param("mediaType", "PHOTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaId").value(100L))
                .andExpect(jsonPath("$.message").value("Uploaded Successfully"));

        verify(transferMediaCache).evict(transferId);
    }

    @Test
    void testGetTransferMediaFromCache() throws Exception {
        Long transferId = 1L;
        Map<String, List<Map<String, Object>>> cachedData = new HashMap<>();
        cachedData.put("images", Collections.emptyList());

        when(transferMediaCache.get(transferId)).thenReturn(cachedData);

        mockMvc.perform(get("/api/transfers/" + transferId + "/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images").exists());

        verify(transferMediaService, never()).getTransferMedia(any());
    }

    @Test
    void testGetTransferMediaFromService() throws Exception {
        Long transferId = 1L;
        Map<String, List<Map<String, Object>>> serviceData = new HashMap<>();
        serviceData.put("images", Collections.emptyList());

        when(transferMediaCache.get(transferId)).thenReturn(null);
        when(transferMediaService.getTransferMedia(transferId)).thenReturn(serviceData);

        mockMvc.perform(get("/api/transfers/" + transferId + "/media"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images").exists());

        verify(transferMediaCache).put(transferId, serviceData);
    }
}
