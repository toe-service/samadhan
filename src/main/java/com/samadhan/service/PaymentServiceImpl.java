package com.samadhan.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentServiceImpl {

    @Value("${pay.key}")
    private String key;

    @Value("${pay.secret}")
    private String secret;

    public RazorpayClient getPaymentClient() throws RazorpayException {
        return new RazorpayClient(key,secret);
    }


}
