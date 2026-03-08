package com.samadhan.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserLoginRequest {

    @NotBlank(message = "email cannot be blank")
    private String username;

    @NotBlank(message = "password cannot be blank")
    private String password;

}
