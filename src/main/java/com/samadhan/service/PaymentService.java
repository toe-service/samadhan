package com.samadhan.service;

import com.samadhan.entity.Subscription;

public interface PaymentService {

	Subscription createPayment(double price, String currency, String method, String intent, String description);

}