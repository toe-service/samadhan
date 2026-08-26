package com.samadhan.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Shared by the user/driver/vehicle forgot-password endpoints — "identifier" is whichever field
// that account type actually logs in with (user email, driver contact number, vehicle username).
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdentifierForgotPasswordRequest {
    public String identifier;
}
