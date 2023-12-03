package com.samadhan.service;

import com.samadhan.entity.Login;
import com.samadhan.exception.ConflictException;
import com.samadhan.repository.LoginRepo;
import com.samadhan.request.LoginRequest;
import com.samadhan.security.TokenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GaadiService {

    @Autowired
    private LoginRepo loginRepo;
    @Autowired
    private TokenApi tokenApi;

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

}
