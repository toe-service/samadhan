//package com.samadhan.service;
//
//import com.google.firebase.auth.FirebaseAuthException;
//import com.samadhan.entity.Login;
//import com.samadhan.exception.NotificationException;
//import com.samadhan.payloads.Payloads;
//import com.samadhan.repository.LoginRepo;
//import com.samadhan.request.LoginRequest;
//import com.samadhan.response.GeneralResponse;
//import com.samadhan.security.TokenApi;
//import com.samadhan.util.Utils;
//import org.json.simple.JSONObject;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.Optional;
//
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//class TestGaadiService {
//
//    @Mock
//    Utils utils;
//    @Mock
//    LoginRepo loginRepo;
//    @Mock
//    TokenApi tokenApi;
//    @InjectMocks
//    private GaadiService gaadiService;
//
//    String token = "jwt-token-string";
//
//    @BeforeEach
//    void pre() throws FirebaseAuthException {
//        LoginRequest loginRequest = Payloads.getLoginRequest();
//        Login loginDetails = Payloads.getLoginDetails(Optional.of(loginRequest.otp));
//        when(loginRepo.findById(loginRequest.mobile)).thenReturn(Optional.of(loginDetails));
//        when(loginRepo.findById("not exist")).thenReturn(Optional.empty());
//        when(loginRepo.findById("wrong otp")).thenReturn(Optional.of(loginDetails));
//        when(tokenApi.generateToken(loginRequest.mobile, 5)).thenReturn(token);
//        when(tokenApi.verifyFirebaseToken("invalid")).thenReturn(false);
//        when(tokenApi.verifyFirebaseToken("valid")).thenReturn(true);
//        when(tokenApi.verifyFirebaseToken("error")).thenThrow(new RuntimeException("error occurred"));
//    }
//
//    @Test
//    void testLogin() throws Exception {
//        LoginRequest loginRequest = Payloads.getLoginRequest();
//        Assertions.assertTrue(gaadiService.login(loginRequest).length() > 1);
//        Assertions.assertEquals("user not found", gaadiService.login(Payloads.getLoginRequestNotExist()));
//        Assertions.assertEquals("wrong otp", gaadiService.login(Payloads.getLoginRequestWrongOtp()));
//
//    }
//
//    @Test
//    void testVerifyToken() throws FirebaseAuthException {
//        GeneralResponse response = gaadiService.verifyToken("invalid");
//        Assertions.assertEquals(401, response.statusCode);
//        response = gaadiService.verifyToken("valid");
//        Assertions.assertEquals(200, response.statusCode);
//        response = gaadiService.verifyToken("error");
//        Assertions.assertEquals(401, response.statusCode);
//        Assertions.assertEquals("error occurred", response.errorMessage);
//    }
//
//    @Test
//    void testSmsService() throws NotificationException {
//        try{
//            gaadiService.sendSms("12345678", "1234");
//        } catch (NotificationException exp) {
//            Assertions.assertTrue(exp.getMessage().contains("Invalid Numbers"));
//        }
//
//
//    }
//}
