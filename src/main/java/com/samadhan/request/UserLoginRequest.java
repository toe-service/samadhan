package com.samadhan.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserLoginRequest {
    public Long mobileNumber;
    public Integer otp;
    public String userLatitude;
    public String userLongitude;

}
