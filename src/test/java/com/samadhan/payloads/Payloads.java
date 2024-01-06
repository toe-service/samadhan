package com.samadhan.payloads;

import com.samadhan.entity.Login;
import com.samadhan.request.LoginRequest;

import java.util.Optional;

public class Payloads {

    public static LoginRequest getLoginRequest() {
        return new LoginRequest("1234567890", "0987");
    }

    public static Login getLoginDetails(Optional<String> otp) {
        Login login = new Login();
        login.setMobile("1234567890");
        login.setOtp(otp.orElse("otp"));
        return login;
    }

}
