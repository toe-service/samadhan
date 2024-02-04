package com.samadhan.exception;

public class NotificationException extends Exception{
    public NotificationException(String msg) {
        super(msg);
    }
    public NotificationException(Exception exp) {
        super(exp);
    }
}
