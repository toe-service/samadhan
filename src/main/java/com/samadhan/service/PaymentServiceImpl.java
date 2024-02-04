package com.samadhan.service;

import com.samadhan.entity.Payment;

public class PaymentServiceImpl implements PaymentService{

	@Override
	public Payment createPayment(double total, String currency, String method, String intent, String description) {
		
		
//		Amount amount = new Amount();
//        amount.setCurrency(currency);
//     //   total = new BigDecimal(total).setScale(2, RoundingMode.HALF_UP).doubleValue();
//        amount.setTotal(String.format("%.2f", total));
//
//        Transaction transaction = new Transaction();
//        transaction.setDescription(description);
//        transaction.setAmount(amount);
//
//        List<Transaction> transactions = new ArrayList<>();
//        transactions.add(transaction);
//
//        Payer payer = new Payer();
//        payer.setPaymentMethod(method.toString());
//
//        Payment payment = new Payment();
//        payment.setIntent(intent.toString());
//        payment.setPayer(payer);  
//        payment.setTransactions(transactions);
//        RedirectUrls redirectUrls = new RedirectUrls();
//        redirectUrls.setCancelUrl(cancelUrl);
//        redirectUrls.setReturnUrl(successUrl);
//        payment.setRedirectUrls(redirectUrls);
//
//        return payment.create(apiContext);
		
		
		
		return null;
	}

}
