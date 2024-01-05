package com.samadhan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.samadhan.entity.Order;
import com.samadhan.entity.Payment;
import com.samadhan.service.PaymentService;

@RestController
@RequestMapping(value = "/pay")
public class PaymentController {

	 @Autowired
	 PaymentService service;
	
	   @PostMapping("/pay")
	    public String payment(@RequestBody Order order) {
	        try {
	            Payment payment = service.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),
	                    order.getIntent(), order.getDescription());
	            
//	            for(Links link:payment.getLinks()) {
//	                if(link.getRel().equals("approval_url")) {
//	                    return "redirect:"+link.getHref();
//	                }
//	            }

	        } catch (Exception e) {

	            e.printStackTrace();
	        }
	        return "redirect:/";
	    }

	
	
	
}
