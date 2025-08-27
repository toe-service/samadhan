package com.samadhan.service;

import com.samadhan.constant.AppConstant;
import com.samadhan.entity.UserLoginEntity;
import com.samadhan.repository.UserLoginRepository;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.response.GetOtpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.IntStream;

@Service
public class LoginService {
    @Autowired
    private UserLoginRepository userLoginRepository;

    @Value("${sms.service.provider.url}")
    private String smsProviderUrl;

    @Value("${sms.client.token}")
    private String smsProviderKey;

    public boolean isOtpValid(UserLoginRequest userLoginRequest) {
        Optional<UserLoginEntity> userLoginData = userLoginRepository.findById(userLoginRequest.mobileNumber);
        return userLoginData
                .map(data -> Objects.equals(data.getOtp(), userLoginRequest.getOtp()))
                .orElse(false);
    }

    public GetOtpResponse generateAndSendOtp(String mobileNumber) {
        try{
//            Integer otp = generateOtp();
//            JSONObject obj = new JSONObject();
//            obj.put("route", "q"); // this will cost 5 rupees per sms
////            obj.put("route", "otp");
//            obj.put("message", "Four digit OTP to login in Toe2Manjil Service is "+otp);
//            obj.put("numbers", mobileNumber);
//            obj.put("authorization", smsProviderKey);
//            obj.put("flash", "0");
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.add("authorization", smsProviderKey);
//            headers.set("Content-Type", "application/json"); // optional - in case you auth in headers
//            HttpEntity<JSONObject> entity = new HttpEntity<>(obj, headers);
//
//
//            //save otp in database
//            UserLoginEntity userLoginEntity = UserLoginEntity.of(Long.parseLong(mobileNumber), otp);
//            userLoginRepository.save(userLoginEntity);
//
//            ResponseEntity<JSONObject> respEntity = new RestTemplate()
//                    .exchange(smsProviderUrl, HttpMethod.POST, entity, JSONObject.class);
//
//            System.out.println("url is " + smsProviderUrl);
//            System.out.println("mobile number and otp is "+mobileNumber+" | "+otp);
//            respEntity.getBody();
            return GetOtpResponse.of(true, AppConstant.OTP_SENT_SUCCESSFUL.replace("<mobile number>", mobileNumber));
        } catch (Exception exp){
            return GetOtpResponse.of(false, AppConstant.OTP_SENT_FAILED.replace("<mobile number>", mobileNumber));
        }
    }

    private Integer generateOtp() {
        Random random = new Random();

        return IntStream.generate(() -> 1000 + random.nextInt(9000))
                .findFirst()
                .getAsInt();

    }

}
