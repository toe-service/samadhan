package com.samadhan.trait;

import com.samadhan.exception.NotificationException;
import org.json.simple.JSONObject;

public interface SmsService {
    JSONObject sendSms(String mobileNumber, String message) throws NotificationException;
}
