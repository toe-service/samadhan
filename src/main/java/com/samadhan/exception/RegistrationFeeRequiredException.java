package com.samadhan.exception;


public class RegistrationFeeRequiredException extends RuntimeException {

    public RegistrationFeeRequiredException(String message) {
        super(message);
    }
}
