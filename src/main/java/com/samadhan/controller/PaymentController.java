package com.samadhan.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
import com.samadhan.enums.DimensionUnit;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.PaymentTypeEnum;
import com.samadhan.enums.SubscriptionPeriodEnum;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.enums.serviceTypeEnum;
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
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;

import javax.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import com.samadhan.exception.ResourceNotFoundException;
import com.samadhan.security.TokenApi;

import java.text.DecimalFormat;

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

	 @Autowired
	 TokenApi tokenApi;

	 // Confirms a payment-verification callback actually came from Razorpay (not a client
	 // fabricating a "success" response without ever paying), using the SDK's own constant-time
	 // HMAC comparison rather than hand-rolling it.
	 private boolean isValidPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
		 try {
			 JSONObject attributes = new JSONObject();
			 attributes.put("razorpay_order_id", razorpayOrderId);
			 attributes.put("razorpay_payment_id", razorpayPaymentId);
			 attributes.put("razorpay_signature", razorpaySignature);
			 return com.razorpay.Utils.verifyPaymentSignature(attributes, "ClYfhcDqxmBDr3ZftMyzuxu1");
		 } catch (Exception e) {
			 return false;
		 }
	 }

	 // Shared by first-purchase (createOrder/verifyPayment) and renew, so both offer the same
	 // 1/3/6/12-month plans at the same price instead of first-purchase being a fixed 3-month deal.
	 private int amountForPlan(String plan) {
		 if (plan == null) return 439900;
		 if (plan.equalsIgnoreCase("MONTHLY")) return 149900;
		 if (plan.equalsIgnoreCase("THREE_MONTH")) return 439900;
		 if (plan.equalsIgnoreCase("SIX_MONTH")) return 859900;
		 if (plan.equalsIgnoreCase("TWELVE_MONTH")) return 1649900;
		 return 439900;
	 }

	 private void applyPlanToSubscription(Subscription subscription, String plan, LocalDate startDate) {
		 if ("MONTHLY".equalsIgnoreCase(plan)) {
			 subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.MONTHLY);
			 subscription.setEndDate(startDate.plusMonths(1));
		 } else if ("SIX_MONTH".equalsIgnoreCase(plan)) {
			 subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.HALFYEAR);
			 subscription.setEndDate(startDate.plusMonths(6));
		 } else if ("TWELVE_MONTH".equalsIgnoreCase(plan)) {
			 subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.YEARLY);
			 subscription.setEndDate(startDate.plusMonths(12));
		 } else {
			 subscription.setSubscriptionPeriod(SubscriptionPeriodEnum.QUARTER);
			 subscription.setEndDate(startDate.plusMonths(3));
		 }
		 subscription.setPaymentType(PaymentTypeEnum.SILVER);
		 subscription.setStartDate(startDate);
	 }



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
	         @RequestParam Long transferId, HttpServletRequest httpRequest) throws Exception {

	     TransferRequestDetails transfer = transferRepository.findById(transferId)
	             .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));

	     TransferVendor sellerVendor = transfer.getTransferVendor();
	     if (sellerVendor == null) {
	         throw new ResourceNotFoundException(
	                 "This request has no vendor assigned yet — an invoice can only be generated once a vendor has taken this ride.");
	     }

	     // Only the vendor who actually fulfilled this ride can pull its invoice — otherwise any
	     // authenticated vendor could download any other vendor's customer/revenue data by ID.
	     String authHeader = httpRequest.getHeader("Authorization");
	     String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
	     Long tokenVendorId = jwt != null ? tokenApi.extractUserId(jwt) : null;
	     if (tokenVendorId == null || !tokenVendorId.equals(sellerVendor.getId())) {
	         throw new AccessDeniedException("You are not authorized to view this invoice");
	     }

	     ByteArrayOutputStream baos = new ByteArrayOutputStream();

	     PdfWriter writer = new PdfWriter(baos);
	     PdfDocument pdf = new PdfDocument(writer);
	     Document document = new Document(pdf, PageSize.A4);
	     document.setMargins(30, 36, 30, 36);

	     PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
	     PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
	     DeviceRgb brand = new DeviceRgb(37, 99, 235);
	     DeviceRgb lightBand = new DeviceRgb(245, 247, 250);
	     DecimalFormat money = new DecimalFormat("#,##0.00");

	     String vendorName = sellerVendor.getVendorName();
	     String vendorAddress = sellerVendor.getVendorAddress();
	     String vendorGst = sellerVendor.getGstNumber();
	     String vendorContact = sellerVendor.getVendorContactNumber();
	     boolean vendorGstRegistered = vendorGst != null && !vendorGst.isBlank();

	     //================ HEADER BAND ===================

	     Table headerBand = new Table(UnitValue.createPercentArray(new float[]{60, 40}));
	     headerBand.setWidth(UnitValue.createPercentValue(100));

	     Cell headerLeft = new Cell().setBorder(Border.NO_BORDER);
	     headerLeft.add(new Paragraph(safeText(vendorName)).setFont(bold).setFontSize(20).setFontColor(brand));
	     headerLeft.add(new Paragraph(safeText(vendorAddress)).setFont(normal).setFontSize(9).setFontColor(ColorConstants.DARK_GRAY));
	     if (vendorContact != null && !vendorContact.isBlank()) {
	         headerLeft.add(new Paragraph("Contact: " + vendorContact).setFont(normal).setFontSize(9).setFontColor(ColorConstants.DARK_GRAY));
	     }

	     Cell headerRight = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT);
	     headerRight.add(new Paragraph("TAX INVOICE").setFont(bold).setFontSize(18).setFontColor(brand));
	     headerRight.add(new Paragraph("Invoice No: INV-" + transfer.getId()).setFont(normal).setFontSize(9));
	     headerRight.add(new Paragraph("Invoice Date: " + LocalDate.now()).setFont(normal).setFontSize(9));

	     headerBand.addCell(headerLeft);
	     headerBand.addCell(headerRight);
	     document.add(headerBand);

	     document.add(new LineSeparator(new SolidLine(1.2f))
	             .setMarginTop(6).setMarginBottom(12).setStrokeColor(brand));

	     //================ SELLER / BUYER =================

	     Table top = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
	     top.setWidth(UnitValue.createPercentValue(100));

	     Cell seller = new Cell().setBackgroundColor(lightBand).setPadding(10).setBorder(Border.NO_BORDER);
	     seller.add(new Paragraph("Service Provider (Seller)").setFont(bold).setFontSize(10));
	     seller.add(new Paragraph(safeText(vendorName)).setFont(normal).setFontSize(9));
	     seller.add(new Paragraph(safeText(vendorAddress)).setFont(normal).setFontSize(9));
	     seller.add(new Paragraph("GSTIN: " + (vendorGstRegistered ? vendorGst : "Not Registered")).setFont(normal).setFontSize(9));
	     seller.add(new Paragraph("SAC Code: 9965 (Goods Transport by Road)").setFont(normal).setFontSize(9));

	     Cell buyer = new Cell().setBackgroundColor(lightBand).setPadding(10).setBorder(Border.NO_BORDER);
	     buyer.add(new Paragraph("Billed To (Customer)").setFont(bold).setFontSize(10));
	     buyer.add(new Paragraph("Name: " + safeText(transfer.getUserDetails() != null ? transfer.getUserDetails().getUserName() : null)).setFont(normal).setFontSize(9));
	     buyer.add(new Paragraph("Mobile: " + safeText(transfer.getUserDetails() != null ? transfer.getUserDetails().getUserContactNumber() : null)).setFont(normal).setFontSize(9));
	     buyer.add(new Paragraph("Pickup: " + safeText(transfer.getSource())).setFont(normal).setFontSize(9));
	     buyer.add(new Paragraph("Destination: " + safeText(transfer.getDestination())).setFont(normal).setFontSize(9));

	     top.addCell(seller);
	     top.addCell(buyer);
	     document.add(top);

	     document.add(new Paragraph("\n"));

	     //================ SUPPLY DETAILS =================

	     Table invoiceInfo = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
	     invoiceInfo.setWidth(UnitValue.createPercentValue(100));
	     invoiceInfo.addCell(plainCell("Pickup Date: " + safeText(transfer.getPickupDate() != null ? transfer.getPickupDate().toString() : null), normal));
	     invoiceInfo.addCell(plainCell("Pickup Slot: " + safeText(transfer.getPickupSchedule()), normal));
	     invoiceInfo.addCell(plainCell("Place of Supply: " + safeText(vendorAddress), normal));
	     invoiceInfo.addCell(plainCell("Reverse Charge Applicable: No", normal));
	     document.add(invoiceInfo);

	     document.add(new Paragraph("\n"));

	     //================ ITEM TABLE =================

	     double rideCharges = nz(transfer.getRideWithoutTaxCalculation());
	     double packaging = nz(transfer.getPackagingCost());
	     double loadingUnloading = nz(transfer.getLoadingUnloading());
	     double taxableValue = rideCharges + packaging + loadingUnloading;
	     double totalGst = nz(transfer.getGstCost());
	     double cgst = totalGst / 2.0;
	     double sgst = totalGst / 2.0;
	     double grandTotal = transfer.getRideCost();

	     Table table = new Table(UnitValue.createPercentArray(new float[]{6, 40, 12, 18}));
	     table.setWidth(UnitValue.createPercentValue(100));

	     table.addHeaderCell(headerCell("S.No", bold, brand));
	     table.addHeaderCell(headerCell("Description", bold, brand));
	     table.addHeaderCell(headerCell("SAC", bold, brand));
	     table.addHeaderCell(headerCell("Amount (Rs.)", bold, brand));

	     int sno = 1;
	     table.addCell(dataCell(String.valueOf(sno++), normal, TextAlignment.CENTER));
	     table.addCell(dataCell("Ride Charges", normal, TextAlignment.LEFT));
	     table.addCell(dataCell("9965", normal, TextAlignment.CENTER));
	     table.addCell(dataCell(money.format(rideCharges), normal, TextAlignment.RIGHT));

	     if (packaging > 0) {
	         table.addCell(dataCell(String.valueOf(sno++), normal, TextAlignment.CENTER));
	         table.addCell(dataCell("Packaging Charges", normal, TextAlignment.LEFT));
	         table.addCell(dataCell("9965", normal, TextAlignment.CENTER));
	         table.addCell(dataCell(money.format(packaging), normal, TextAlignment.RIGHT));
	     }

	     if (loadingUnloading > 0) {
	         table.addCell(dataCell(String.valueOf(sno++), normal, TextAlignment.CENTER));
	         table.addCell(dataCell("Loading / Unloading", normal, TextAlignment.LEFT));
	         table.addCell(dataCell("9965", normal, TextAlignment.CENTER));
	         table.addCell(dataCell(money.format(loadingUnloading), normal, TextAlignment.RIGHT));
	     }

	     document.add(table);

	     document.add(new Paragraph("\n"));

	     //================ TAX SUMMARY =================

	     Table totals = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
	     totals.setWidth(UnitValue.createPercentValue(100));
	     totals.setHorizontalAlignment(HorizontalAlignment.RIGHT);

	     totals.addCell(totalsCell("Taxable Value", normal, false));
	     totals.addCell(totalsCell(money.format(taxableValue), normal, false));

	     totals.addCell(totalsCell("CGST @ 9%", normal, false));
	     totals.addCell(totalsCell(money.format(cgst), normal, false));

	     totals.addCell(totalsCell("SGST @ 9%", normal, false));
	     totals.addCell(totalsCell(money.format(sgst), normal, false));

	     totals.addCell(totalsCell("Grand Total", bold, true));
	     totals.addCell(totalsCell(money.format(grandTotal), bold, true));

	     document.add(totals);

	     document.add(new Paragraph("\nNote: Tax shown as CGST + SGST, assuming intra-state supply.")
	             .setFont(normal).setFontSize(8).setFontColor(ColorConstants.GRAY));

	     document.add(new Paragraph("\n\n"));

	     //================ FOOTER =================

	     Paragraph sign = new Paragraph("For " + safeText(vendorName))
	             .setFont(bold)
	             .setTextAlignment(TextAlignment.RIGHT);

	     document.add(sign);

	     document.add(new Paragraph("\n\n"));

	     document.add(new Paragraph("Authorized Signatory")
	             .setFont(normal)
	             .setFontSize(9)
	             .setTextAlignment(TextAlignment.RIGHT));

	     document.add(new Paragraph("\nThis is a system-generated invoice.")
	             .setFont(normal).setFontSize(7).setFontColor(ColorConstants.GRAY)
	             .setTextAlignment(TextAlignment.CENTER));

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

	 private double nz(Double value) {
	     return value == null ? 0.0 : value;
	 }

	 private String safeText(String value) {
	     return value == null || value.isBlank() ? "-" : value;
	 }

	 private Cell plainCell(String text, PdfFont font) {
	     return new Cell().setBorder(Border.NO_BORDER).add(new Paragraph(text).setFont(font).setFontSize(9));
	 }

	 private Cell headerCell(String text, PdfFont bold, DeviceRgb color) {
	     return new Cell()
	             .setBackgroundColor(color)
	             .add(new Paragraph(text).setFont(bold).setFontColor(ColorConstants.WHITE).setFontSize(9));
	 }

	 private Cell dataCell(String text, PdfFont font, TextAlignment align) {
	     return new Cell()
	             .setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
	             .setTextAlignment(align)
	             .add(new Paragraph(text).setFont(font).setFontSize(9));
	 }

	 private Cell totalsCell(String text, PdfFont font, boolean emphasized) {
	     Cell cell = new Cell()
	             .setBorder(Border.NO_BORDER)
	             .setTextAlignment(TextAlignment.RIGHT)
	             .add(new Paragraph(text).setFont(font).setFontSize(emphasized ? 11 : 9));
	     if (emphasized) {
	         cell.setBorderTop(new SolidBorder(ColorConstants.BLACK, 0.75f));
	     }
	     return cell;
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
            @RequestParam(required = false) String cc,
            @RequestParam(required = false, defaultValue = "INCH") DimensionUnit dimensionUnit,
            @RequestParam(required = false, defaultValue = "true") Boolean isMovable) {

	    try {

			RideCostSummary rideCostCalculation = paymentService.getrideCostCalculation(pickuplatitude, pickuplongitude,
					destinationlatitude, destinationlongitude, parcelType, carModel, bikeModel, parcelWeight, cc,
					length, width, heigth, dimensionUnit, isMovable);

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
	
	
	@GetMapping("/bookVehicleCostList")
	public ResponseEntity<?> bookVehicleCostList(
	        @RequestParam Double pickuplatitude,
	        @RequestParam Double pickuplongitude,
	        @RequestParam Double destinationlatitude,
	        @RequestParam Double destinationlongitude,
	        @RequestParam(required = false, defaultValue = "false") Boolean helperRequired,
	        @RequestParam(required = false, defaultValue = "0") Integer helperCount,
	        @RequestParam(required = false) VehicleCategoryEnum vehicleCategory,
	        @RequestParam(required = false) serviceTypeEnum  serviceType,
	        @RequestParam(required = false) String  packingType,
	        @RequestParam(required = false) String  homeType,
	        @RequestParam(required = false, defaultValue = "0") Integer fromFloor,
	        @RequestParam(required = false, defaultValue = "false") Boolean liftAvailable) throws JsonMappingException, JsonProcessingException {

//		return ResponseEntity.ok(paymentService.getVehicleCostList(pickuplatitude, pickuplongitude, destinationlatitude,
//				destinationlongitude, helperRequired, helperCount, vehicleCategory, fromFloor, liftAvailable));
		
		return ResponseEntity.ok(paymentService.getVehicleCostList(pickuplatitude, pickuplongitude, destinationlatitude,
				destinationlongitude, helperRequired, helperCount, vehicleCategory, serviceType, homeType, packingType,
				fromFloor, liftAvailable));
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

	    if (!isValidPaymentSignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
	        return ResponseEntity
	                .badRequest()
	                .body("Invalid Signature");
	    }

	    LocalDate localDate = LocalDate.now();
	    LocalDate oneMonthsLater = localDate.plusMonths(1);

//	    Date date = Date.from(
//	        localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
//	    );
	    Optional<TransferVendor> vendor=transferVendorRepo.findById(req.getVendorId());
	    // Reuse the existing subscription row if one already exists (e.g. the trial created
	    // automatically at registration) rather than always inserting a new one — Subscription
	    // is meant to be one-per-vendor (@OneToOne), and a second row makes every subsequent
	    // findByVendorId() call (buy/renew/view) throw IncorrectResultSizeDataAccessException.
	    Subscription subscription = paymentRepo.findByVendorId(req.getVendorId());
	    if (subscription == null) {
	        subscription = new Subscription();
	        subscription.setVendor(vendor.get());
	    }
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
	        @RequestParam Long vendorId,
	        @RequestParam(required = false) String plan) throws Exception {

	    JSONObject options = new JSONObject();

	    options.put("amount", amountForPlan(plan));
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

	    if (!isValidPaymentSignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
	        return ResponseEntity
	                .badRequest()
	                .body("Invalid Signature");
	    }

	    LocalDate localDate = LocalDate.now();
	    String plan = req.getPlan();

	    Optional<TransferVendor> vendor=transferVendorRepo.findById(req.getVendorId());
	    Subscription subscription=paymentRepo.findByVendorId(req.getVendorId());
	    if (subscription == null) {
	        subscription = new Subscription();
	        subscription.setVendor(vendor.get());
	    }
	    applyPlanToSubscription(subscription, plan, localDate);

	    paymentRepo.save(subscription);

	    transferVendorRepo.activateVendor(
	            req.getVendorId(),3);

	    WalletTransaction walletTransaction=new WalletTransaction();
	    walletTransaction.setAmount(amountForPlan(plan) / 100.0);
	    walletTransaction.setVendor(vendor.get());
	    walletTransaction.setTransactionType("Subscription Purchased");
	    walletTransactionRepository.save(walletTransaction);

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
	        @RequestParam Long vendorId,
	        @RequestParam String plan) throws RazorpayException {
		
		  JSONObject options = new JSONObject();

		  options.put("amount", amountForPlan(plan));

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

	    if (!isValidPaymentSignature(req.getRazorpayOrderId(), req.getRazorpayPaymentId(), req.getRazorpaySignature())) {
	        return ResponseEntity
	                .badRequest()
	                .body("Invalid Signature");
	    }

	    LocalDate localDate = LocalDate.now();

	    String plan = req.getPlan();

	    Optional<TransferVendor> vendor=transferVendorRepo.findById(req.getVendorId());
	    Subscription subscription=paymentRepo.findByVendorId(req.getVendorId());
	    if (subscription == null) {
	        subscription = new Subscription();
	        subscription.setVendor(vendor.get());
	    }

	    // If the old subscription already lapsed, renewing from oldEndDate+1 (in the past)
	    // would silently shortchange the vendor by however many days it's been expired —
	    // start from whichever is later: today, or the day after the old period ended.
	    LocalDate startDate = (subscription.getEndDate() != null && subscription.getEndDate().plusDays(1).isAfter(localDate))
	            ? subscription.getEndDate().plusDays(1)
	            : localDate;

	    applyPlanToSubscription(subscription, plan, startDate);

	    paymentRepo.save(subscription);

	    transferVendorRepo.activateVendor(
	            req.getVendorId(),3);

	    WalletTransaction walletTransaction=new WalletTransaction();
	    walletTransaction.setAmount(amountForPlan(plan) / 100.0);
	    walletTransaction.setVendor(vendor.get());
	    walletTransaction.setTransactionType("Subscription Renewed Purchased");
	    walletTransaction.setCreatedDate(localDate);
	    walletTransactionRepository.save(walletTransaction);


	    return ResponseEntity.ok(
	            "Subscription Renewed");
	}
	
	
}