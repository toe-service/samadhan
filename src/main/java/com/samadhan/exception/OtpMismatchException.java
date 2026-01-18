package com.samadhan.exception;

public class OtpMismatchException extends Exception{
    public OtpMismatchException(String msg) {
        super(msg);
    }
    public OtpMismatchException(Exception exp) {
        super(exp);
    }
}
