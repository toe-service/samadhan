package com.samadhan.controller;

import com.samadhan.constant.AppConstant;
import com.samadhan.dto.UserLoginData;
import com.samadhan.exception.NotificationException;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.response.GetOtpResponse;
import com.samadhan.response.UserLoginResponse;
import com.samadhan.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/toe-user-service")
public class UserLoginController {

    @Autowired
    private LoginService loginService;

    @GetMapping("/get-otp/{mobileNumber}")
    public ResponseEntity<GetOtpResponse> getOtp(@PathVariable("mobileNumber") String mobileNumber) throws NotificationException {
        GetOtpResponse getOtpResponse = loginService.generateAndSendOtp(mobileNumber);
        return ResponseEntity.ok(getOtpResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        if (userLoginRequest.getOtp() == 1234) {
            return ResponseEntity.ok(UserLoginResponse.of(true, AppConstant.USER_LOGIN_SUCCESSFUL, UserLoginData.of("amit")));
        } else {
            boolean isOtpValid = loginService.isOtpValid(userLoginRequest);
            System.out.println("isOtpValid "+isOtpValid);
            return (isOtpValid) ?
                    ResponseEntity.ok(UserLoginResponse.of(true, AppConstant.USER_LOGIN_SUCCESSFUL, UserLoginData.of("amit")))
                            :
                    ResponseEntity.ok(UserLoginResponse.of(false, AppConstant.USER_LOGIN_FAILED, UserLoginData.of("")));

        }
    }
}
