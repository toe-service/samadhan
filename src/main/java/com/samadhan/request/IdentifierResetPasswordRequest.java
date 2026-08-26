package com.samadhan.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Shared by the user/driver/vehicle reset-password endpoints — see IdentifierForgotPasswordRequest.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifierResetPasswordRequest {
    public String identifier;
    public Integer otp;
    public String newPassword;
}
