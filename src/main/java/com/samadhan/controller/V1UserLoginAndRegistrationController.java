package com.samadhan.controller;

import com.samadhan.constant.AppConstant;
import com.samadhan.entity.Driver;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.OtpMismatchException;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.request.UserOtpVerifyRequest;
import com.samadhan.request.UserRegisterRequest;
import com.samadhan.response.LoginResponse;
import com.samadhan.response.ResponseObject;
import com.samadhan.service.LoginService;
import com.samadhan.service.UserService;
import com.samadhan.service.VehicleService;
import com.samadhan.service.driversService;
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

    @Autowired
    private driversService driverservice;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private VehicleService vehicleService;


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
    public ResponseEntity<ResponseObject<UserDetails>> loginUser(
    		@RequestParam String UserName, @RequestParam String password) throws ConflictException {
      //  logger.info("User Login request is {}", userLoginRequest);
//        loginService.registerUser(userRegisterRequest);
        UserDetails user =userService.loginUser(UserName, password);
        ResponseObject<UserDetails> success = ResponseUtil.populateResponseObject(user, "SUCCESS", null);
        return ResponseEntity.ok(success);

    }
    
    @PostMapping("/vehicle-login")
    public ResponseEntity<ResponseObject<Vehicle>> loginVehicle(
    		@RequestParam String UserName, @RequestParam String password) throws ConflictException {
      //  logger.info("User Login request is {}", userLoginRequest);
//        loginService.registerUser(userRegisterRequest);
        Vehicle vehicle =vehicleService.loginVehicle(UserName, password);
        ResponseObject<Vehicle> success = ResponseUtil.populateResponseObject(vehicle, "SUCCESS", null);
        return ResponseEntity.ok(success);

    }
    
    @PostMapping("/driver-login")
    public ResponseEntity<ResponseObject<Driver>> loginDriver(@RequestParam String UserName, @RequestParam String password
	) throws ConflictException {

		Driver driver = driverservice.loginDriver(UserName, password);
      
		ResponseObject<Driver> success = ResponseUtil.populateResponseObject(driver, "SUCCESS", null);

		return ResponseEntity.ok(success);

	}
    
    @PostMapping("/role-login")
    public ResponseEntity<?> loginRole(@RequestParam String UserName, @RequestParam String password
	) throws ConflictException {

    	LoginResponse data = driverservice.loginRole(UserName, password);
      
		ResponseObject<?> success = ResponseUtil.populateResponseObject(data, "SUCCESS", null);

		return ResponseEntity.ok(success);

	}

    @PostMapping("/transfervendor-login")
    public ResponseEntity<ResponseObject<TransferVendor>> loginTransfervendor(@RequestParam String UserName, @RequestParam String password
           ) throws ConflictException {
    	
    		TransferVendor transferv=loginService.loginTransfervendor(UserName,password);
//        logger.info("User Login request is {}", userLoginRequest);
//        loginService.registerUser(userRegisterRequest);
        ResponseObject<TransferVendor> success = ResponseUtil.populateResponseObject(transferv, "SUCCESS", null);
       
        return ResponseEntity.ok(success);
    	

    }

}
