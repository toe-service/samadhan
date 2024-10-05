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

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(value = "/pay")
public class PaymentController {

	@Autowired
	private PaymentServiceImpl paymentService;

	 @Autowired
	 private ObjectMapper mapper;


	@PostMapping("/generate-new-invoice")
	public Object generateNewInvoice(@RequestBody PaymentInvoiceRequest request) throws RazorpayException {
		System.out.println("invoice  request "+request);
		RazorpayClient razorpayClient = Utils.getPaymentClient();
		JSONObject requestJson = getRequest(request);
		System.out.println("request json is "+requestJson);
		com.razorpay.Invoice invoice = razorpayClient.invoices.create(requestJson);
		System.out.println("invoice is "+invoice);
		return ""+invoice.toJson();
	}

	private JSONObject getRequest(PaymentInvoiceRequest request) {
		JSONObject invoiceRequest = new JSONObject();
		invoiceRequest.put("type", request.getType());
		invoiceRequest.put("description", request.getDescription());
		invoiceRequest.put("partial_payment",true);
		JSONObject customer = new JSONObject();
		customer.put("name",request.getCustomer().getName());
		customer.put("contact",request.getCustomer().getContact());
		customer.put("email",request.getCustomer().getEmail());
		JSONObject billingAddress = new JSONObject();
		billingAddress.put("line1",request.getCustomer().getBillingAddress().getLine1());
		billingAddress.put("line2", request.getCustomer().getBillingAddress().getLine2());
		billingAddress.put("zipcode",request.getCustomer().getBillingAddress().getZipcode());
		billingAddress.put("city",request.getCustomer().getBillingAddress().getCity());
		billingAddress.put("state",request.getCustomer().getBillingAddress().getState());
		billingAddress.put("country",request.getCustomer().getBillingAddress().getCountry());
		customer.put("billing_address",billingAddress);
		JSONObject shippingAddress = new JSONObject();
		shippingAddress.put("line1",request.getCustomer().getShippingAddress().getLine1());
		shippingAddress.put("line2",request.getCustomer().getShippingAddress().getLine2());
		shippingAddress.put("zipcode",request.getCustomer().getShippingAddress().getZipcode());
		shippingAddress.put("city",request.getCustomer().getShippingAddress().getCity());
		shippingAddress.put("state",request.getCustomer().getShippingAddress().getState());
		shippingAddress.put("country",request.getCustomer().getShippingAddress().getCountry());
		customer.put("shipping_address",shippingAddress);
		invoiceRequest.put("customer",customer);
		List<Object> lines = new ArrayList<>();
		JSONObject lineItems = new JSONObject();
		lineItems.put("name",request.getLineItems().get(0).getName());
		lineItems.put("description",request.getLineItems().get(0).getDescription());
		lineItems.put("amount",request.getLineItems().get(0).getAmount());
		lineItems.put("currency",request.getLineItems().get(0).getCurrency());
		lineItems.put("quantity",request.getLineItems().get(0).getQuantity());
		lines.add(lineItems);
		invoiceRequest.put("line_items",lines);
		invoiceRequest.put("email_notify", 1);
		invoiceRequest.put("sms_notify", 1);
		invoiceRequest.put("currency","INR");
		invoiceRequest.put("expire_by", 2180479824L);
		return invoiceRequest;
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