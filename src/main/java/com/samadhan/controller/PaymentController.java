package com.samadhan.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.type.Date;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.response.Error;
import com.samadhan.response.ResponseObject;
import com.samadhan.response.SubscriptionResponse;
import com.samadhan.service.PaymentServiceImpl;
import com.samadhan.util.ResponseUtil;
import com.samadhan.util.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.samadhan.dto.PaymentVerificationRequest;
import com.samadhan.dto.RideCostSummary;
import com.samadhan.dto.WalletPaymentRequest;
import com.samadhan.dto.payment.PaymentInvoiceRequest;
import com.samadhan.entity.Subscription;
import com.samadhan.entity.TransferRequestDetails;
import com.samadhan.entity.TransferVendor;
import com.samadhan.entity.VendorWallet;
import com.samadhan.entity.WalletTransaction;
import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.PaymentTypeEnum;
import com.samadhan.enums.SubscriptionPeriodEnum;
import com.samadhan.repository.PaymentRepository;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VendorWalletRepository;
import com.samadhan.repository.WalletTransactionRepo;
import com.samadhan.service.PaymentService;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;



@RestController
@RequestMapping(value = "/pay")
public class PaymentController {

	@Autowired
	private PaymentServiceImpl paymentService;

	 @Autowired
	 private ObjectMapper mapper;
	 
	 @Autowired
	 TransferRequestRepository transferRepository;
	 
	 @Autowired
	 VendorWalletRepository  VendorWalletRepo;
	 
	 @Autowired
	 WalletTransactionRepo walletTransactionRepository;
	 
	 @Autowired
	 TransferVendorRepository transferVendorRepo;
	 
	 @Autowired
	 PaymentRepository paymentRepo;



//	@PostMapping("/generate-new-invoice")
//	public Object generateNewInvoice(@RequestBody PaymentInvoiceRequest request) throws RazorpayException {
//		System.out.println("invoice  request "+request);
//		RazorpayClient razorpayClient = Utils.getPaymentClient();
//		JSONObject requestJson = getRequest(request);
//		System.out.println("request json is "+requestJson);
//		com.razorpay.Invoice invoice = razorpayClient.invoices.create(requestJson);
//		System.out.println("invoice is "+invoice);
//		return ""+invoice.toJson();
//	}
	
//	  @GetMapping("/generateInvoice")
//	    public ResponseEntity<byte[]> generateInvoice(
//	            @RequestParam Long transferId) throws Exception {
//
//	        TransferRequestDetails transfer =
//	                transferRepository.findById(transferId)
//	                        .orElseThrow(() ->
//	                                new RuntimeException("Transfer not found"));
//
//	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//	        PdfWriter writer = new PdfWriter(baos);
//	        PdfDocument pdfDocument = new PdfDocument(writer);
//	        Document document = new Document(pdfDocument);
//
//	        document.add(new Paragraph("TransferEaze Invoice")
//	                .setBold()
//	                .setFontSize(20));
//
//	        document.add(new Paragraph("Invoice No: INV-" + transfer.getId()));
//	        document.add(new Paragraph("Date: " + LocalDate.now()));
//
//	        document.add(new Paragraph(" "));
//
//	        Table table = new Table(2);
//
//	        table.addCell("Transfer ID");
//	        table.addCell(String.valueOf(transfer.getId()));
//
//	        table.addCell("Customer");
//	        table.addCell(
//	                transfer.getUserDetails().getUserName()
//	        );
//
//	        table.addCell("Source");
//	        table.addCell(transfer.getSource());
//
//	        table.addCell("Destination");
//	        table.addCell(transfer.getDestination());
//
//	        table.addCell("Status");
//	        table.addCell(transfer.getTransferStatus().name());
//
//	        table.addCell("Amount");
//	        table.addCell("₹" + transfer.getRideCost());
//
//	        document.add(table);
//
//	        document.add(new Paragraph(" "));
//	        document.add(new Paragraph(
//	                "Thank you for choosing TransferEaze."
//	        ));
//
//	        document.close();
//
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.setContentType(MediaType.APPLICATION_PDF);
//
//	        headers.setContentDisposition(
//	                ContentDisposition.builder("attachment")
//	                        .filename(
//	                                "Invoice_" + transferId + ".pdf"
//	                        )
//	                        .build()
//	        );
//
//	        return ResponseEntity.ok()
//	                .headers(headers)
//	                .body(baos.toByteArray());
//	    }
	 
	 
	 @GetMapping("/generateInvoice")
	 public ResponseEntity<byte[]> generateInvoice(
	         @RequestParam Long transferId) throws Exception {

	     TransferRequestDetails transfer = transferRepository.findById(transferId)
	             .orElseThrow(() -> new RuntimeException("Transfer not found"));

	     ByteArrayOutputStream baos = new ByteArrayOutputStream();

	     PdfWriter writer = new PdfWriter(baos);
	     PdfDocument pdf = new PdfDocument(writer);
	     Document document = new Document(pdf);

	     PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
	     PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

	     //================ HEADER ===================

	     Paragraph title = new Paragraph("TransferEaze")
	             .setFont(bold)
	             .setFontSize(24)
	             .setTextAlignment(TextAlignment.CENTER);

	     Paragraph invoiceTitle = new Paragraph("TAX INVOICE")
	             .setFont(bold)
	             .setFontSize(16)
	             .setTextAlignment(TextAlignment.CENTER);

	     document.add(title);
	     document.add(invoiceTitle);

	     document.add(new Paragraph("\n"));

	     //================ COMPANY & CUSTOMER =================

	     Table top = new Table(UnitValue.createPercentArray(new float[]{50,50}));
	     top.setWidth(UnitValue.createPercentValue(100));

	     Cell seller = new Cell();

	     seller.add(new Paragraph("Sold By").setFont(bold));
	     seller.add(new Paragraph("TransferEaze Technologies Pvt Ltd"));
	     seller.add(new Paragraph("Lucknow, Uttar Pradesh"));
	     seller.add(new Paragraph("GSTIN : 09XXXXXXXXXXXX"));
	     seller.add(new Paragraph("PAN : ABCDE1234F"));

	     Cell buyer = new Cell();

	     buyer.add(new Paragraph("Billing Address").setFont(bold));
	     buyer.add(new Paragraph("Customer : " + transfer.getUserDetails().getUserName()));
	     buyer.add(new Paragraph("Mobile : " + transfer.getUserDetails().getUserContactNumber()));
	     buyer.add(new Paragraph("Pickup : " + transfer.getSource()));
	     buyer.add(new Paragraph("Destination : " + transfer.getDestination()));

	     top.addCell(seller);
	     top.addCell(buyer);

	     document.add(top);

	     document.add(new Paragraph("\n"));

	     //================ INVOICE DETAILS =================

	     Table invoiceInfo = new Table(UnitValue.createPercentArray(new float[]{50,50}));
	     invoiceInfo.setWidth(UnitValue.createPercentValue(100));

	     invoiceInfo.addCell(new Cell().add(new Paragraph("Invoice No : INV-" + transfer.getId())));
	     invoiceInfo.addCell(new Cell().add(new Paragraph("Invoice Date : " + LocalDate.now())));

	     invoiceInfo.addCell(new Cell().add(new Paragraph("Pickup Date : " + transfer.getPickupDate())));
	     invoiceInfo.addCell(new Cell().add(new Paragraph("Pickup Slot : " + transfer.getPickupSchedule())));

	     document.add(invoiceInfo);

	     document.add(new Paragraph("\n"));

	     //================ ITEM TABLE =================

	     Table table = new Table(UnitValue.createPercentArray(new float[]{1,5,2}));
	     table.setWidth(UnitValue.createPercentValue(100));

	     table.addHeaderCell(new Cell().add(new Paragraph("S.No").setFont(bold)));
	     table.addHeaderCell(new Cell().add(new Paragraph("Description").setFont(bold)));
	     table.addHeaderCell(new Cell().add(new Paragraph("Amount").setFont(bold)));

	     table.addCell("1");
	     table.addCell("Ride Charges");
	     table.addCell("₹" + transfer.getRideWithoutTaxCalculation());

	     table.addCell("2");
	     table.addCell("Packaging Charges");
	     table.addCell("₹" + transfer.getPackagingCost());

	     table.addCell("3");
	     table.addCell("Loading / Unloading");
	     table.addCell("₹" + transfer.getLoadingUnloading());

	     table.addCell("4");
	     table.addCell("GST (18%)");
	     table.addCell("₹" + transfer.getGstCost());

	     document.add(table);

	     document.add(new Paragraph("\n"));

	     //================ TOTAL =================

	     Table total = new Table(UnitValue.createPercentArray(new float[]{70,30}));
	     total.setWidth(UnitValue.createPercentValue(100));

	     total.addCell(new Cell().add(new Paragraph("Grand Total").setFont(bold)));
	     total.addCell(new Cell().add(new Paragraph("₹" + transfer.getRideCost()).setFont(bold)));

	     document.add(total);

	     document.add(new Paragraph("\n"));

	     //================ AMOUNT IN WORDS =================

	     document.add(new Paragraph("Amount In Words")
	             .setFont(bold));

	//     document.add(new Paragraph(convertAmountToWords(transfer.getRideCost()) + " Only"));

	     document.add(new Paragraph("\n\n"));

	     //================ FOOTER =================

	     Paragraph sign = new Paragraph("For TransferEaze")
	             .setFont(bold)
	             .setTextAlignment(TextAlignment.RIGHT);

	     document.add(sign);

	     document.add(new Paragraph("\n\n"));

	     document.add(new Paragraph("Authorized Signatory")
	             .setTextAlignment(TextAlignment.RIGHT));

	     document.close();

	     HttpHeaders headers = new HttpHeaders();
	     headers.setContentType(MediaType.APPLICATION_PDF);

	     headers.setContentDisposition(
	             ContentDisposition.builder("attachment")
	                     .filename("Invoice_" + transferId + ".pdf")
	                     .build());

	     return ResponseEntity.ok()
	             .headers(headers)
	             .body(baos.toByteArray());
	 }
	 


//	private JSONObject getRequest(PaymentInvoiceRequest request) {
//		JSONObject invoiceRequest = new JSONObject();
//		invoiceRequest.put("type", request.getType());
//		invoiceRequest.put("description", request.getDescription());
//		invoiceRequest.put("partial_payment",true);
//		JSONObject customer = new JSONObject();
//		customer.put("name",request.getCustomer().getName());
//		customer.put("contact",request.getCustomer().getContact());
//		customer.put("email",request.getCustomer().getEmail());
//		JSONObject billingAddress = new JSONObject();
//		billingAddress.put("line1",request.getCustomer().getBillingAddress().getLine1());
//		billingAddress.put("line2", request.getCustomer().getBillingAddress().getLine2());
//		billingAddress.put("zipcode",request.getCustomer().getBillingAddress().getZipcode());
//		billingAddress.put("city",request.getCustomer().getBillingAddress().getCity());
//		billingAddress.put("state",request.getCustomer().getBillingAddress().getState());
//		billingAddress.put("country",request.getCustomer().getBillingAddress().getCountry());
//		customer.put("billing_address",billingAddress);
//		JSONObject shippingAddress = new JSONObject();
//		shippingAddress.put("line1",request.getCustomer().getShippingAddress().getLine1());
//		shippingAddress.put("line2",request.getCustomer().getShippingAddress().getLine2());
//		shippingAddress.put("zipcode",request.getCustomer().getShippingAddress().getZipcode());
//		shippingAddress.put("city",request.getCustomer().getShippingAddress().getCity());
//		shippingAddress.put("state",request.getCustomer().getShippingAddress().getState());
//		shippingAddress.put("country",request.getCustomer().getShippingAddress().getCountry());
//		customer.put("shipping_address",shippingAddress);
//		invoiceRequest.put("customer",customer);
//		List<Object> lines = new ArrayList<>();
//		JSONObject lineItems = new JSONObject();
//		lineItems.put("name",request.getLineItems().get(0).getName());
//		lineItems.put("description",request.getLineItems().get(0).getDescription());
//		lineItems.put("amount",request.getLineItems().get(0).getAmount());
//		lineItems.put("currency",request.getLineItems().get(0).getCurrency());
//		lineItems.put("quantity",request.getLineItems().get(0).getQuantity());
//		lines.add(lineItems);
//		invoiceRequest.put("line_items",lines);
//		invoiceRequest.put("email_notify", 1);
//		invoiceRequest.put("sms_notify", 1);
//		invoiceRequest.put("currency","INR");
//		invoiceRequest.put("expire_by", 2180479824L);
//		return invoiceRequest;
//	}

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
	
	@GetMapping("/subscriptions/{vendorId}")
	public ResponseEntity<Subscription> getSubscriptionsByVendor(@PathVariable Long vendorId) {
		Subscription vendorSubscriptions = paymentService.getSubscriptionsByVendor(vendorId);
		return ResponseEntity.ok(vendorSubscriptions);
	}

//	@GetMapping("/rideCostCalculation")
//	public ResponseEntity<ResponseObject<RideCostSummary>> getrideCostCalculation(@RequestParam String pickuplatitude,
//            @RequestParam String pickuplongitude,
//            @RequestParam String destinationlatitude,
//            @RequestParam String destinationlongitude,
//            @RequestParam ParcelTypeEnum parcelType,
//            @RequestParam(required = false) CarModelEnum carModel,
//            @RequestParam(required = false) BikeModelEnum bikeModel,
//            @RequestParam(required = false) Double parcelWeight,
//            @RequestParam(required = false) Double length,
//            @RequestParam(required = false) Double width,
//            @RequestParam(required = false) Double heigth,
//            @RequestParam(required = false) String cc) {
//		try {
//		RideCostSummary rideCostCalculation = paymentService.getrideCostCalculation(pickuplatitude, pickuplongitude, destinationlatitude, destinationlongitude, parcelType, carModel, bikeModel, parcelWeight, cc, length, width, heigth);
//		
//		return ResponseEntity.ok(ResponseUtil.populateResponseObject(rideCostCalculation, "SUCCESS", null));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
	
	@GetMapping("/rideCostCalculation")
	public ResponseEntity<ResponseObject<RideCostSummary>> getrideCostCalculation(@RequestParam String pickuplatitude,
            @RequestParam String pickuplongitude,
            @RequestParam String destinationlatitude,
            @RequestParam String destinationlongitude,
            @RequestParam ParcelTypeEnum parcelType,
            @RequestParam(required = false) CarModelEnum carModel,
            @RequestParam(required = false) BikeModelEnum bikeModel,
            @RequestParam(required = false) Double parcelWeight,
            @RequestParam(required = false) Double length,
            @RequestParam(required = false) Double width,
            @RequestParam(required = false) Double heigth,
            @RequestParam(required = false) String cc) {

	    try {

			RideCostSummary rideCostCalculation = paymentService.getrideCostCalculation(pickuplatitude, pickuplongitude,
					destinationlatitude, destinationlongitude, parcelType, carModel, bikeModel, parcelWeight, cc,
					length, width, heigth);

	        return ResponseEntity.ok(
	                ResponseUtil.populateResponseObject(
	                        rideCostCalculation,
	                        "SUCCESS",
	                        null));

	    } catch (Exception e) {
	    	  Error error=new Error("Server", e.getMessage());
	    	    error.setIdentifier("Server");
	    	    error.setMessage(e.getMessage());
	    	
	        return ResponseEntity.badRequest().body(
	                ResponseUtil.populateResponseObject(
	                        null,
	                        "FAILED",
	                        error));
	    }
	}
	
	@PostMapping("/free-subscription/createOrder")
	public ResponseEntity<?> createFreeSubscriptionOrder(
	        @RequestParam Long vendorId) throws Exception {

	    JSONObject options = new JSONObject();

	    options.put("amount", 100); // ₹999
	    options.put("currency", "INR");
	    options.put("receipt", "subscription_" + vendorId);

	    RazorpayClient client =
	            new RazorpayClient("rzp_test_SxdhjKRBQOSQoN", "ClYfhcDqxmBDr3ZftMyzuxu1");

	    Order order = client.orders.create(options);

	    return ResponseEntity.ok(order.toString());
	}
	
	@PostMapping("/free-subscription/verifyPayment")
	@Transactional
	public ResponseEntity<?> verifyFreeSubscriptionPayment(
	        @RequestBody PaymentVerificationRequest req)
	        throws Exception {

	    String payload =
	        req.getRazorpayOrderId()
	        + "|"
	        + req.getRazorpayPaymentId();

//	    String generatedSignature =
//	            calculateHmacSHA256(
//	                    payload,
//	                    "ClYfhcDqxmBDr3ZftMyzuxu1");
//
//	    if(!generatedSignature.equals(
//	            req.getRazorpaySignature())) {
//
//	        return ResponseEntity
//	                .badRequest()
//	                .body("Invalid Signature");
//	    }
	    LocalDate localDate = LocalDate.now();
	    LocalDate oneMonthsLater = localDate.plusMonths(1);

//	    Date date = Date.from(
//	        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
//	    );
	    Optional<TransferVendor> vendor=transferVendorRepo.findById(req.getVendorId());
	    Subscription subscription=new Subscription();
	    subscription.setVendor(vendor.get());
	    subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.Free);
	    subscription.setPaymentType(PaymentTypeEnum.Free);
	    subscription.setStartDate(localDate);
	    subscription.setEndDate(oneMonthsLater);
	    
	    paymentRepo.save(subscription);
	    
	    transferVendorRepo.activateVendor(
	            req.getVendorId(),1);
	    
	    WalletTransaction walletTransaction=new WalletTransaction();
	    walletTransaction.setAmount(1.0);
	    walletTransaction.setVendor(vendor.get());
	    walletTransaction.setTransactionType("Subscription Purchased");
	    
	    walletTransactionRepository.save(walletTransaction);

	    return ResponseEntity.ok(
	            "Subscription Activated");
	}
	
	@PostMapping("/subscription/createOrder")
	public ResponseEntity<?> createOrder(
	        @RequestParam Long vendorId) throws Exception {

	    JSONObject options = new JSONObject();

	    options.put("amount", 99900); // ₹999
	    options.put("currency", "INR");
	    options.put("receipt", "subscription_" + vendorId);

	    RazorpayClient client =
	            new RazorpayClient("rzp_test_SxdhjKRBQOSQoN", "ClYfhcDqxmBDr3ZftMyzuxu1");

	    Order order = client.orders.create(options);

	    return ResponseEntity.ok(order.toString());
	}
	
	@PostMapping("/subscription/verifyPayment")
	@Transactional
	public ResponseEntity<?> verifyPayment(
	        @RequestBody PaymentVerificationRequest req)
	        throws Exception {

	    String payload =
	        req.getRazorpayOrderId()
	        + "|"
	        + req.getRazorpayPaymentId();

//	    String generatedSignature =
//	            calculateHmacSHA256(
//	                    payload,
//	                    "ClYfhcDqxmBDr3ZftMyzuxu1");
//
//	    if(!generatedSignature.equals(
//	            req.getRazorpaySignature())) {
//
//	        return ResponseEntity
//	                .badRequest()
//	                .body("Invalid Signature");
//	    }
	    LocalDate localDate = LocalDate.now();
	    LocalDate threeMonthsLater = localDate.plusMonths(3);

//	    Date date = Date.from(
//	        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
//	    );
	    
	    
	    Optional<TransferVendor> vendor=transferVendorRepo.findById(req.getVendorId());
	    Subscription subscription=paymentRepo.findByVendorId(req.getVendorId());
	    //subscription.setVendor(vendor.get());
	    subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.QUARTER);
	    subscription.setPaymentType(PaymentTypeEnum.SILVER);
	    subscription.setStartDate(localDate);
	    subscription.setEndDate(threeMonthsLater);
	    
	    paymentRepo.save(subscription);
	    
	    transferVendorRepo.activateVendor(
	            req.getVendorId(),3);
	    
	    WalletTransaction walletTransaction=new WalletTransaction();
	    walletTransaction.setAmount(999.0);
	    walletTransaction.setVendor(vendor.get());
	    walletTransaction.setTransactionType("Subscription Purchased");

	    return ResponseEntity.ok(
	            "Subscription Activated");
	}
	
	
	@PostMapping("/wallet/create-order")
	public Map<String,Object> createOrder(
	        @RequestParam Long vendorId,
	        @RequestParam Double amount) throws Exception {

	    RazorpayClient client =
	        new RazorpayClient("rzp_test_SxdhjKRBQOSQoN", "ClYfhcDqxmBDr3ZftMyzuxu1");

	    JSONObject orderRequest = new JSONObject();
	    orderRequest.put("amount", (int)(amount * 100));
	    orderRequest.put("currency", "INR");
	    orderRequest.put("receipt", "wallet_" + vendorId);

	    Order order = client.orders.create(orderRequest);

	    Map<String,Object> response = new HashMap<>();
	    response.put("orderId", order.get("id"));
	    response.put("amount", amount);

	    return response;
	}

	
	@PostMapping("/wallet/payment-success")
	public String paymentSuccess(
	        @RequestBody WalletPaymentRequest request)
	        throws Exception {

	    VendorWallet wallet =
	            VendorWalletRepo.findByVendor(
	                    request.getVendorId());
	    
	    Optional<TransferVendor> vendor=transferVendorRepo.findById(request.getVendorId());

	    if (wallet == null) {
	    	
//	        throw new RuntimeException(
//	                "Wallet not found");
	    	 wallet = new VendorWallet();
	    	 wallet.setBalance(request.getAmount());
	    }else {

	    wallet.setBalance(
	            wallet.getBalance()
	            + request.getAmount());
	    }
	    wallet.setVendor(vendor.get());
	    VendorWalletRepo.save(wallet);

	    WalletTransaction txn =
	            new WalletTransaction();

	    txn.setAmount(request.getAmount());
	    txn.setTransactionType("CREDIT");
//	    txn.setReferenceId(
//	            Long.parseLong(request.getRazorpayPaymentId()));
	    txn.setDescription(
	            "Wallet Recharge");

	    txn.setVendor(wallet.getVendor());

	    walletTransactionRepository.save(txn);

	    return "SUCCESS";
	}
	
	@PostMapping("/subscription/renew")
	public ResponseEntity<String> renewSubscription(
	        @RequestParam Long vendorId) throws RazorpayException {
		
		  JSONObject options = new JSONObject();

		    options.put("amount", 99900); // ₹999
		    options.put("currency", "INR");
		    options.put("receipt", "subscription_" + vendorId);

		    RazorpayClient client =
		            new RazorpayClient("rzp_test_SxdhjKRBQOSQoN", "ClYfhcDqxmBDr3ZftMyzuxu1");

		    Order order = client.orders.create(options);

		    return ResponseEntity.ok(order.toString());

	   // paymentService.renewSubscription(vendorId);

	   // return ResponseEntity.ok("Subscription renewed successfully");
	}
	
	
	@PostMapping("/renew-subscription/verifyPayment")
	@Transactional
	public ResponseEntity<?> verifyRenewSubscriptionPayment(
	        @RequestBody PaymentVerificationRequest req)
	        throws Exception {

	    String payload =
	        req.getRazorpayOrderId()
	        + "|"
	        + req.getRazorpayPaymentId();
	    
	    LocalDate localDate = LocalDate.now();
	    
	    Optional<TransferVendor> vendor=transferVendorRepo.findById(req.getVendorId());
	    Subscription subscription=paymentRepo.findByVendorId(req.getVendorId());
	    
	    LocalDate startDate=subscription.getEndDate().plusDays(1);
	    LocalDate threeMonthsLater = startDate.plusMonths(3);
	    
	    //subscription.setVendor(vendor.get());
	    subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.QUARTER);
	    subscription.setPaymentType(PaymentTypeEnum.SILVER);
	    subscription.setStartDate(startDate);
	    subscription.setEndDate(threeMonthsLater);
	    
	    paymentRepo.save(subscription);
	    
	    transferVendorRepo.activateVendor(
	            req.getVendorId(),3);
	    
	    WalletTransaction walletTransaction=new WalletTransaction();
	    walletTransaction.setAmount(999.0);
	    walletTransaction.setVendor(vendor.get());
	    walletTransaction.setTransactionType("Subscription Renewed Purchased");
	    walletTransaction.setCreatedDate(localDate);
	    

	    return ResponseEntity.ok(
	            "Subscription Renewed");
	}
	
	
}