package com.samadhan.service;

import com.samadhan.entity.Login;
import com.samadhan.payloads.Payloads;
import com.samadhan.repository.LoginRepo;
import com.samadhan.request.LoginRequest;
import com.samadhan.security.TokenApi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.mockito.Mockito.when;

@SpringBootTest
class TestGaadiService {

    @Mock
    LoginRepo loginRepo;
    @Mock
    TokenApi tokenApi;
    @InjectMocks
    private GaadiService gaadiService;

    @Test
    void testLoginGetToken() throws Exception {
        String token = "jwt-token-string";
        LoginRequest loginRequest = Payloads.getLoginRequest();
        String mobile = loginRequest.mobile;
        String otp = loginRequest.otp;
        Login loginDetails = Payloads.getLoginDetails(Optional.of(otp));
        when(loginRepo.findById(mobile)).thenReturn(Optional.of(loginDetails));
        when(tokenApi.generateToken(mobile, 5)).thenReturn(token);
        Assertions.assertEquals(token, gaadiService.login(loginRequest));
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        String mobile = Payloads.getLoginRequest().mobile;
        when(loginRepo.findById(mobile)).thenReturn(Optional.empty());
        Assertions.assertEquals("user not found", gaadiService.login(Payloads.getLoginRequest()));
    }

    @Test
    void testLoginWrongOtp() throws Exception {
        String mobile = Payloads.getLoginRequest().mobile;
        Login loginDetails = Payloads.getLoginDetails(Optional.of("1234"));
        when(loginRepo.findById(mobile)).thenReturn(Optional.of(loginDetails));
        Assertions.assertEquals("wrong otp", gaadiService.login(Payloads.getLoginRequest()));
    }
}
