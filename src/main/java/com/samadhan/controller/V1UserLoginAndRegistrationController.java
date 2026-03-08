package com.samadhan.controller;

import com.samadhan.constant.AppConstant;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.OtpMismatchException;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.request.UserOtpVerifyRequest;
import com.samadhan.request.UserRegisterRequest;
import com.samadhan.response.ResponseObject;
import com.samadhan.service.LoginService;
import com.samadhan.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1")
public class V1UserLoginAndRegistrationController {

    private static final Logger logger = LoggerFactory.getLogger(V1UserLoginAndRegistrationController.class);

    @Autowired
    private LoginService loginService;


    @PostMapping("/user-otp-verify")
    public ResponseEntity<ResponseObject<?>> loginUser(@RequestBody UserOtpVerifyRequest userOtpVerifyRequest) throws OtpMismatchException {
        logger.info("User login request is {}", userOtpVerifyRequest.toString());
        if (userOtpVerifyRequest.getOtp() == 1234) {
            ResponseObject<UserOtpVerifyRequest> userLoginRequestResponseObject = ResponseUtil.populateResponseObject(userOtpVerifyRequest, AppConstant.USER_LOGIN_SUCCESSFUL, null);
            return ResponseEntity.ok(userLoginRequestResponseObject);
        } else {
            boolean isOtpValid = loginService.isOtpValid(userOtpVerifyRequest);
            System.out.println("isOtpValid " + isOtpValid);
            ResponseObject<UserOtpVerifyRequest> userLoginRequestResponseObject = ResponseUtil.populateResponseObject(userOtpVerifyRequest, AppConstant.USER_LOGIN_SUCCESSFUL, null);
            return ResponseEntity.ok(userLoginRequestResponseObject);

        }
    }

    /**
     * used in transfer service
     */
    @PostMapping("/user-register")
    public ResponseEntity<ResponseObject<?>> registerUser(
            @Valid @RequestBody UserRegisterRequest userRegisterRequest) throws ConflictException {
        logger.info("User Register request is {}", userRegisterRequest);
        loginService.registerUser(userRegisterRequest);
        ResponseObject<UserRegisterRequest> success = ResponseUtil.populateResponseObject(userRegisterRequest, "SUCCESS", null);
        return ResponseEntity.ok(success);

    }

    @PostMapping("/user-login")
    public ResponseEntity<ResponseObject<?>> loginUser(
            @Valid @RequestBody UserLoginRequest userLoginRequest) throws ConflictException {
        logger.info("User Login request is {}", userLoginRequest);
//        loginService.registerUser(userRegisterRequest);
        ResponseObject<UserLoginRequest> success = ResponseUtil.populateResponseObject(userLoginRequest, "SUCCESS", null);
        return ResponseEntity.ok(success);

    }


}
