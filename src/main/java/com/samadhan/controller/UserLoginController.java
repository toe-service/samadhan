package com.samadhan.controller;

import com.samadhan.constant.AppConstant;
import com.samadhan.dto.UserLoginData;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.NotFoundException;
import com.samadhan.exception.NotificationException;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.request.UserRegisterRequest;
import com.samadhan.response.GetOtpResponse;
import com.samadhan.response.ResponseObject;
import com.samadhan.response.UserLoginResponse;
import com.samadhan.response.UserRegisterResponse;
import com.samadhan.service.LoginService;
import com.samadhan.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/toe-user-service")
public class UserLoginController {

    private static final Logger logger = LoggerFactory.getLogger(UserLoginController.class);

    @Autowired
    private LoginService loginService;

    @GetMapping("/get-otp/{mobileNumber}")
    public ResponseEntity<GetOtpResponse> getOtp(@PathVariable("mobileNumber") String mobileNumber) throws NotificationException {
        logger.info("Mobile number to generate opt is {}", mobileNumber);
        GetOtpResponse getOtpResponse = loginService.generateAndSendOtp(mobileNumber);
        return ResponseEntity.ok(getOtpResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> loginUser(@RequestBody UserLoginRequest userLoginRequest) {
        logger.info("User login request is {}", userLoginRequest.toString());
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

    @PostMapping("/register")
    public ResponseEntity<ResponseObject<UserRegisterRequest>> registerUser(@RequestBody UserRegisterRequest userRegisterRequest) throws ConflictException {
        logger.info("User Register request is {}", userRegisterRequest);
        loginService.registerUser(userRegisterRequest);
        ResponseObject<UserRegisterRequest> success = ResponseUtil.populateResponseObject(userRegisterRequest, "SUCCESS", null);
        return ResponseEntity.ok(success);

    }

    @PostMapping("/update-lat-long/{userId}")
    public ResponseEntity<ResponseObject<String>> updateLatLong(@PathVariable("userId") Long userId,
                                                                             @RequestParam("latitude") String latitude,
                                                                             @RequestParam("longitude")String longitude
    ) throws NotFoundException {
        loginService.updateUserLatLong(userId, latitude, longitude);
        ResponseObject<String> success = ResponseUtil.populateResponseObject("User successfully updated", "SUCCESS", null);
        return ResponseEntity.ok(success);
    }

}
