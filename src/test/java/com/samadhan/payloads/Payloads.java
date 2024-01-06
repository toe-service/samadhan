package com.samadhan.payloads;

import com.samadhan.request.LoginRequest;

public class Payloads {

    public static LoginRequest getLoginRequest() {
        return new LoginRequest("1234567890", "0987");
    }
}
