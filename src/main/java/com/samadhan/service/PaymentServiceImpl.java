package com.samadhan.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.enums.SubscriptionPrice;
import com.samadhan.response.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component
public class PaymentServiceImpl {

    @Value("${pay.key}")
    private String key;

    @Value("${pay.secret}")
    private String secret;

    public RazorpayClient getPaymentClient() throws RazorpayException {
        return new RazorpayClient(key,secret);
    }


    public List<SubscriptionResponse> getAllSubscriptions() {
        return Arrays.stream(SubscriptionPrice.values())
                .map(obj -> new SubscriptionResponse(obj.getSubscriptionName(), obj.getPrice(), Collections.emptyList()))
                .toList();
    }

}