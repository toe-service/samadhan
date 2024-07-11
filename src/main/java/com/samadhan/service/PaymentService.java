package com.samadhan.service;

import com.samadhan.entity.Payment;

public interface PaymentService {

	Payment createPayment(double price, String currency, String method, String intent, String description);

}