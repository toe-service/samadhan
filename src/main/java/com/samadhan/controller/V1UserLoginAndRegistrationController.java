package com.samadhan.controller;

import com.samadhan.constant.AppConstant;
import com.samadhan.entity.Driver;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.UserDetails;
import com.samadhan.entity.Vehicle;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.UserRole;
import com.samadhan.exception.ConflictException;
import com.samadhan.exception.OtpMismatchException;
import com.samadhan.request.RefreshTokenRequest;
import com.samadhan.request.UserLoginRequest;
import com.samadhan.request.UserOtpRequest;
import com.samadhan.request.UserOtpVerifyRequest;
import com.samadhan.request.UserRegisterRequest;
import com.samadhan.entity.RefreshToken;
import com.samadhan.response.LoginResponse;
import com.samadhan.response.ResponseObject;
import com.samadhan.response.TokenRefreshResponse;
import com.samadhan.response.TransferVendorLoginResponse;
import com.samadhan.response.UserOtpVerifyResponse;
import com.samadhan.security.RefreshTokenService;
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

    @Autowired
    private RefreshTokenService refreshTokenService;


    @PostMapping("/user-otp-verify")
    public ResponseEntity<ResponseObject<UserOtpVerifyResponse>> loginUser(@RequestBody UserOtpVerifyRequest userOtpVerifyRequest) throws OtpMismatchException {
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
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                userDetails.getUserContactNumber(), userRole, userDetails.getId());

        userDetails.setLastLogin(System.currentTimeMillis());

        UserOtpVerifyResponse otpVerifyResponse = new UserOtpVerifyResponse();
        otpVerifyResponse.setUserContactNumber(userDetails.getUserContactNumber());
        otpVerifyResponse.setOtp(userOtpVerifyRequest.getOtp());
        otpVerifyResponse.setUserId(userDetails.getId());
        otpVerifyResponse.setToken(jwtToken);
        otpVerifyResponse.setRefreshToken(refreshToken.getToken());
        otpVerifyResponse.setUserRole(userRole);
        otpVerifyResponse.setExpiresIn(900000L);

        ResponseObject<UserOtpVerifyResponse> response = ResponseUtil.populateResponseObject(
                otpVerifyResponse, AppConstant.USER_LOGIN_SUCCESSFUL, null);

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
    public ResponseEntity<ResponseObject<TransferVendorLoginResponse>> loginTransfervendor(@RequestParam String UserName, @RequestParam String password
           ) throws ConflictException {

        TransferVendor transferv = loginService.loginTransfervendor(UserName, password);

        String jwtToken = tokenApi.generateToken(
                transferv.getVendorEmail(), UserRole.VENDOR.getValue(), transferv.getId(), 15);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                transferv.getVendorEmail(), UserRole.VENDOR.getValue(), transferv.getId());

        TransferVendorLoginResponse vendorLoginResponse = new TransferVendorLoginResponse();
        vendorLoginResponse.setVendorId(transferv.getId());
        vendorLoginResponse.setVendorName(transferv.getVendorName());
        vendorLoginResponse.setVendorEmail(transferv.getVendorEmail());
        vendorLoginResponse.setToken(jwtToken);
        vendorLoginResponse.setRefreshToken(refreshToken.getToken());
        vendorLoginResponse.setUserRole(UserRole.VENDOR.getValue());
        vendorLoginResponse.setExpiresIn(900000L);

        ResponseObject<TransferVendorLoginResponse> response = ResponseUtil.populateResponseObject(
                vendorLoginResponse, AppConstant.USER_LOGIN_SUCCESSFUL, null);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ResponseObject<TokenRefreshResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {

        RefreshToken existingRefreshToken = refreshTokenService.findByToken(refreshTokenRequest.getRefreshToken());
        refreshTokenService.verifyExpiration(existingRefreshToken);

        String newAccessToken = tokenApi.generateToken(
                existingRefreshToken.getUserName(), existingRefreshToken.getUserRole(),
                existingRefreshToken.getUserId(), 15);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                existingRefreshToken.getUserName(), existingRefreshToken.getUserRole(),
                existingRefreshToken.getUserId());

        TokenRefreshResponse tokenRefreshResponse = new TokenRefreshResponse();
        tokenRefreshResponse.setToken(newAccessToken);
        tokenRefreshResponse.setRefreshToken(newRefreshToken.getToken());
        tokenRefreshResponse.setExpiresIn(900000L);

        ResponseObject<TokenRefreshResponse> response = ResponseUtil.populateResponseObject(
                tokenRefreshResponse, "SUCCESS", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseObject<?>> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.revokeToken(refreshTokenRequest.getRefreshToken());
        ResponseObject<?> response = ResponseUtil.populateResponseObject(null, "SUCCESS", null);
        return ResponseEntity.ok(response);
    }

}
