package com.samadhan.response;

import com.samadhan.dto.UserLoginData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class UserLoginResponse {
    private boolean status;
    private String message;
    private UserLoginData data;
}
