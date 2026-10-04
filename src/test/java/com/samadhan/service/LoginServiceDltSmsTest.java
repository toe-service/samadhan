package com.samadhan.service;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import com.samadhan.repository.DriverRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.UserRepository;
import com.samadhan.repository.VehicleRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginServiceDltSmsTest {

    @Mock private RestTemplate restTemplate;
    @Mock private UserRepository userRepository;
    @Mock private TransferVendorRepository transferVendorRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginService, "smsProviderUrl", "https://www.fast2sms.com/dev/bulkV2");
        ReflectionTestUtils.setField(loginService, "smsProviderKey", "test-api-key-from-config");
        ReflectionTestUtils.setField(loginService, "fast2smsSenderId", "TRNEZE");
        ReflectionTestUtils.setField(loginService, "fast2smsMessageId", "226883");
        ReflectionTestUtils.setField(loginService, "fast2smsRoute", "dlt");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JSONObject.class)))
                .thenReturn(ResponseEntity.ok(new JSONObject()));
    }

    @SuppressWarnings("unchecked")
    private HttpEntity<JSONObject> captureRequest() {
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), captor.capture(), eq(JSONObject.class));
        return captor.getValue();
    }

    @Test
    void route_isDlt() {
        loginService.generateAndSendOtp("9876543210");
        assertEquals("dlt", captureRequest().getBody().get("route"));
    }

    @Test
    void senderId_isTrneze() {
        loginService.generateAndSendOtp("9876543210");
        assertEquals("TRNEZE", captureRequest().getBody().get("sender_id"));
    }

    @Test
    void messageId_is226883() {
        loginService.generateAndSendOtp("9876543210");
        assertEquals("226883", captureRequest().getBody().get("message"));
    }

    @Test
    void variablesValues_matchesReturnedOtp() {
        Integer otp = loginService.generateAndSendOtp("9876543210");
        assertEquals(String.valueOf(otp), captureRequest().getBody().get("variables_values"));
    }

    @Test
    void numbers_isRecipientMobile() {
        loginService.generateAndSendOtp("9876543210");
        assertEquals("9876543210", captureRequest().getBody().get("numbers"));
    }

    @Test
    void apiKey_isInAuthorizationHeaderNotBody() {
        loginService.generateAndSendOtp("9876543210");
        HttpEntity<JSONObject> req = captureRequest();
        assertEquals("test-api-key-from-config", req.getHeaders().getFirst("authorization"));
        assertFalse(req.getBody().containsKey("authorization"));
    }

    @Test
    void generateAndSendOtp_callsRestTemplateExactlyOnce() {
        loginService.generateAndSendOtp("9876543210");
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JSONObject.class));
    }

    @Test
    void fast2smsFailure_propagatesToCaller() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(JSONObject.class)))
                .thenThrow(HttpServerErrorException.InternalServerError.class);
        assertThrows(RuntimeException.class, () -> loginService.generateAndSendOtp("9876543210"));
    }
}
