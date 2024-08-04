package com.samadhan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.response.SubscriptionResponse;
import com.samadhan.service.PaymentServiceImpl;
import com.samadhan.util.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.samadhan.dto.payment.PaymentInvoiceRequest;
import com.samadhan.service.PaymentService;

import java.util.List;

@RestController
@RequestMapping(value = "/pay")
public class PaymentController {

	@Autowired
	private PaymentServiceImpl paymentService;

	 @Autowired
	 private ObjectMapper mapper;


	@PostMapping("/generate-new-invoice")
	public JSONObject generateNewInvoice(@RequestBody PaymentInvoiceRequest request) throws RazorpayException {
		JSONObject requestJson = mapper.convertValue(request, JSONObject.class);
		RazorpayClient razorpayClient = Utils.getPaymentClient();
		com.razorpay.Invoice invoice = razorpayClient.invoices.create(requestJson);
		return invoice.toJson();
	}


	@GetMapping("/verify-payment/{invoiceId}")
	public String checkPayment(@PathVariable String invoiceId) throws RazorpayException {
		RazorpayClient razorpayClient = Utils.getPaymentClient();
		com.razorpay.Invoice fetch = razorpayClient.invoices.fetch(invoiceId);
		return "invoice data "+fetch.toJson();
	}


	@GetMapping("/subscriptions")
	public ResponseEntity<List<SubscriptionResponse>> getSubscriptions() {
		List<SubscriptionResponse> allSubscriptions = paymentService.getAllSubscriptions();
		return ResponseEntity.ok(allSubscriptions);
	}

	
	
	
}