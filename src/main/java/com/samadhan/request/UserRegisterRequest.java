package com.samadhan.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Column;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class UserRegisterRequest {
    @NotBlank(message = "contact number cannot be blank")
    private String userContactNumber;

    @NotBlank(message = "email cannot be blank")
    private String userEmail;

    @NotBlank(message = "password cannot be blank")
    private String userPassword;

}
