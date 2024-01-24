package com.samadhan.service;

import com.samadhan.entity.Login;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.NotificationException;
import com.samadhan.repository.LoginRepo;
import com.samadhan.request.LoginRequest;
import com.samadhan.response.GeneralResponse;
import com.samadhan.security.TokenApi;
import com.samadhan.trait.SmsService;
import com.samadhan.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

@Service
public class GaadiService implements SmsService {

    @Autowired
    private LoginRepo loginRepo;
    @Autowired
    private TokenApi tokenApi;
    @Autowired
    private Utils utils;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${sms.service-provider.url}")
    private String smsProviderUrl;
    @Value("${sms.client.token}")
    private String clientToken;

    public String login(LoginRequest loginRequest) throws Exception {
        try {
            Login login = loginRepo.findById(loginRequest.mobile).orElseThrow(() -> new Exception("user not found"));
            if(login.getOtp().equalsIgnoreCase(loginRequest.otp)) return tokenApi.generateToken(loginRequest.mobile, 5);
            else throw new Exception("wrong otp");
        } catch (Exception exp) {
            //log error
            return exp.getMessage();
        }

    }

    public GeneralResponse verifyToken(String token) {
        try {
            if(tokenApi.verifyFirebaseToken(token)) {
                return Utils.getSuccessResponse("token is valid");
            } else {
               return Utils.getUnauthorizeResponse();
            }
        } catch (Exception exp) {
            //log error
            return Utils.getFailureResponse(exp);
        }
    }

    @Override
    public void sendSms(String mobileNumber, String message) throws NotificationException {
        postCallSmsOtp(message, mobileNumber);
    }

    private String postCallSmsOtp(String sms, String mobileNo) throws NotificationException {
        try{
            JSONObject obj = new JSONObject();
            obj.put("route", "otp");
            obj.put("variables_values", sms);
            obj.put("numbers", mobileNo);

            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("authorization", clientToken);
            headers.set("Content-Type", "application/json"); // optional - in case you auth in headers
            HttpEntity<JSONObject> entity = new HttpEntity<>(obj, headers);

            ResponseEntity<String> respEntity = restTemplate.exchange(smsProviderUrl, HttpMethod.POST, entity, String.class);

            System.out.println("otp out is "+respEntity);
            return respEntity.toString();
        } catch (Exception exp) {
            throw new NotificationException(exp);
        }

    }
}
