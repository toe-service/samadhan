package com.samadhan.exception;

public class NotFoundException extends Exception{
    public NotFoundException(String msg) {
        super(msg);
    }
    public NotFoundException(Exception exp) {
        super(exp);
    }
}
