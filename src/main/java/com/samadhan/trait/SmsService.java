package com.samadhan.trait;

import com.samadhan.exception.NotificationException;

public interface SmsService {
    void sendSms(String mobileNumber, String message) throws NotificationException;
}
