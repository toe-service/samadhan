package com.samadhan.util;

public class PasswordUtil {

    // Shared by every login/password-reset path (vendor, user, driver, vehicle) that transparently
    // migrates legacy plaintext passwords to bcrypt on first successful use — see LoginService.
    public static boolean isBcryptHash(String value) {
        return value != null && (value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$"));
    }
}
