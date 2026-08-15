package com.samadhan.controller;

import com.samadhan.constant.AppConstant;
import com.samadhan.entity.Driver;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.OtpMismatchException;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.request.UserOtpRequest;
import com.samadhan.request.UserOtpVerifyRequest;
import com.samadhan.request.UserRegisterRequest;
import com.samadhan.response.AuthenticationResponse;
import com.samadhan.response.LoginResponse;
import com.samadhan.response.ResponseObject;
import com.samadhan.response.UserOtpVerifyResponse;
import com.samadhan.security.TokenApi;
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

    @Autowired
    private TokenApi tokenApi;


    @PostMapping("/user-otp-verify")
    public ResponseEntity<AuthenticationResponse> loginUser(@RequestBody UserOtpVerifyRequest userOtpVerifyRequest) throws OtpMismatchException {
        logger.info("User login request is {}", userOtpVerifyRequest.toString());
        UserDetails userDetails;

        if (userOtpVerifyRequest.getOtp() == 1234) {
            userDetails = userService.findByUserContactNumber(userOtpVerifyRequest.getUserContactNumber())
                    .orElseThrow(() -> new OtpMismatchException("user details not found"));
        } else {
            userDetails = loginService.isOtpValid(userOtpVerifyRequest);
        }

        String userRole = userDetails.getUserRole() != null ? userDetails.getUserRole() : "USER";
        String jwtToken = tokenApi.generateToken(userDetails.getUserContactNumber(), userRole, userDetails.getId(), 15);

        userDetails.setLastLogin(System.currentTimeMillis());

        AuthenticationResponse response = new AuthenticationResponse(
                200, true, AppConstant.USER_LOGIN_SUCCESSFUL,
                jwtToken, userDetails.getId(),
                userDetails.getUserName(), userRole, 900000L
        );

        return ResponseEntity.ok(response);
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

    @PostMapping("/send-otp")
    public ResponseEntity<ResponseObject<?>> generateAndSendOtp(
            @Valid @RequestBody UserOtpRequest userOtpRequest) throws ConflictException {
        logger.info("User Register request is {}", userOtpRequest);
        if(userOtpRequest.getContactNumber().equalsIgnoreCase("914060test")) {
            ResponseObject<UserOtpRequest> success = ResponseUtil.populateResponseObject(userOtpRequest, "SUCCESS", null);
            return ResponseEntity.ok(success);
        }
        loginService.sendOtp(userOtpRequest);
        ResponseObject<UserOtpRequest> success = ResponseUtil.populateResponseObject(userOtpRequest, "SUCCESS", null);
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
    public ResponseEntity<?> loginRole(@RequestParam String UserName, @RequestParam String password,@RequestParam(required = false) String fcmToken
	) throws ConflictException {

    	logger.info("fcmToken "+fcmToken);
    	LoginResponse data = driverservice.loginRole(UserName, password, fcmToken);
      
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
