package com.samadhan.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticationResponse {
    public int statusCode;
    public boolean success;
    public String message;
    public String token;
    public Long userId;
    public String userName;
    public String userRole;
    public Long expiresIn;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(int statusCode, boolean success, String message,
                                  String token, Long userId, String userName,
                                  String userRole, Long expiresIn) {
        this.statusCode = statusCode;
        this.success = success;
        this.message = message;
        this.token = token;
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.expiresIn = expiresIn;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
