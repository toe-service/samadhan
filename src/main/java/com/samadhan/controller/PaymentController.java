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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.samadhan.enums.UserRole;
import com.samadhan.enums.VehicleCategoryEnum;
import com.samadhan.enums.VendorPickupVehicleEnum;
import com.samadhan.enums.serviceTypeEnum;
import com.samadhan.repository.PaymentRepository;
import com.samadhan.repository.TransferRequestRepository;
import com.samadhan.repository.TransferVendorRepository;
import com.samadhan.repository.VendorWalletRepository;
import com.samadhan.repository.WalletTransactionRepo;
import com.samadhan.service.PaymentService;
import com.samadhan.service.StorageService;
import com.samadhan.util.GeoUtils;
import com.samadhan.util.GstInvoiceUtil;

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
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
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
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;



@RestController
@RequestMapping(value = "/pay")
public class PaymentController {

	private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);

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

	 @Autowired
	 StorageService storageService;

	 @Autowired
	 com.samadhan.service.LocationService locationService;

	 // Shared-secret gate for /pay/admin/reconcile-payment — a break-glass manual tool, not a
	 // user-facing feature, so a full admin-role/login system felt like overkill. Blank by default
	 // (same convention as pay.webhook.secret) so the endpoint simply refuses everything until this
	 // is deliberately set.
	 @Value("${admin.reconcile.key:}")
	 private String adminReconcileKey;

	 // Confirms a payment-verification callback actually came from Razorpay (not a client
	 // fabricating a "success" response without ever paying), using the SDK's own constant-time
	 // HMAC comparison rather than hand-rolling it.
	 private boolean isValidPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) {
		 try {
			 JSONObject attributes = new JSONObject();
			 attributes.put("razorpay_order_id", razorpayOrderId);
			 attributes.put("razorpay_payment_id", razorpayPaymentId);
			 attributes.put("razorpay_signature", razorpaySignature);
			 return com.razorpay.Utils.verifyPaymentSignature(attributes, paymentService.getSecret());
		 } catch (Exception e) {
			 logger.warn("Payment signature verification failed for order {}: {}", razorpayOrderId, e.getMessage());
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
	 @Transactional
	 public ResponseEntity<byte[]> generateInvoice(
	         @RequestParam Long transferId, HttpServletRequest httpRequest) throws Exception {

	     TransferRequestDetails transfer = transferRepository.findById(transferId)
	             .orElseThrow(() -> new ResourceNotFoundException("Transfer not found with id: " + transferId));

	     TransferVendor sellerVendor = transfer.getTransferVendor();
	     if (sellerVendor == null) {
	         throw new ResourceNotFoundException(
	                 "This request has no vendor assigned yet — an invoice can only be generated once a vendor has taken this ride.");
	     }

	     // Only the vendor who actually fulfilled this ride, or the customer who booked it, can pull
	     // this invoice — otherwise any authenticated vendor/user could download someone else's
	     // customer/revenue data by ID.
	     String authHeader = httpRequest.getHeader("Authorization");
	     String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
	     Long tokenUserId = jwt != null ? tokenApi.extractUserId(jwt) : null;
	     String tokenRole = jwt != null ? tokenApi.extractUserRole(jwt) : null;

	     boolean isFulfillingVendor = UserRole.VENDOR.getValue().equalsIgnoreCase(tokenRole)
	             && tokenUserId != null && tokenUserId.equals(sellerVendor.getId());
	     boolean isBookingCustomer = UserRole.USER.getValue().equalsIgnoreCase(tokenRole)
	             && tokenUserId != null && transfer.getUserDetails() != null
	             && tokenUserId.equals(transfer.getUserDetails().getId());

	     if (!isFulfillingVendor && !isBookingCustomer) {
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

	     // CGST+SGST vs IGST: compare the vendor's own state (from their GSTIN's state code) against
	     // the pickup location's state (place of supply for an unregistered/individual recipient is
	     // where the goods are handed over for transport, not the vendor's own address — Section
	     // 12(8), IGST Act). If either state can't be confidently determined (no GSTIN, geocoding
	     // failed, coordinates missing), fail open to CGST+SGST rather than guessing IGST — that
	     // matches today's existing (pre-this-change) behavior for the common case, rather than
	     // silently switching tax heads on an assumption.
	     String vendorState = vendorGstRegistered ? GstInvoiceUtil.stateForGstin(vendorGst) : null;
	     String pickupState = null;
	     Double pickupLat = GeoUtils.parseCoord(transfer.getSourceLatitude());
	     Double pickupLng = GeoUtils.parseCoord(transfer.getSourceLongitude());
	     if (vendorGstRegistered && pickupLat != null && pickupLng != null) {
	         pickupState = locationService.getState(pickupLat, pickupLng);
	     }
	     boolean interState = vendorGstRegistered && vendorState != null && pickupState != null
	             && !vendorState.equalsIgnoreCase(pickupState);

	     // Sequential per-vendor GST document numbering, assigned once and reused on every
	     // re-download — see TransferRequestDetails#invoiceNumber and TransferVendor#invoiceSequence.
	     String invoiceNumber = transfer.getInvoiceNumber();
	     if (invoiceNumber == null) {
	         String financialYear = GstInvoiceUtil.currentFinancialYear(LocalDate.now());
	         if (!financialYear.equals(sellerVendor.getInvoiceFinancialYear())) {
	             sellerVendor.setInvoiceFinancialYear(financialYear);
	             sellerVendor.setInvoiceSequence(0);
	         }
	         int nextSequence = sellerVendor.getInvoiceSequence() + 1;
	         sellerVendor.setInvoiceSequence(nextSequence);
	         transferVendorRepo.save(sellerVendor);

	         invoiceNumber = String.format("INV/%s/%05d", financialYear, nextSequence);
	         transfer.setInvoiceNumber(invoiceNumber);
	         transferRepository.save(transfer);
	     }

	     // Loaded once here and embedded in the footer below; a fetch failure shouldn't block
	     // invoice generation, so the invoice still renders (without a signature image) if the
	     // stored key is missing or the bucket read fails.
	     byte[] signatureBytes = null;
	     String signatureStorageKey = sellerVendor.getSignatureStorageKey();
	     if (signatureStorageKey != null && !signatureStorageKey.isBlank()) {
	         try {
	             signatureBytes = storageService.getObjectAsBytes(signatureStorageKey);
	         } catch (Exception e) {
	             logger.warn("Failed to load signature image for vendor {}: {}", sellerVendor.getId(), e.getMessage());
	         }
	     }

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
	     // Rule 49, CGST Rules: an unregistered supplier can't charge GST, so they issue a "Bill of
	     // Supply", not a "Tax Invoice" — only a GST-registered vendor's document is a real tax invoice.
	     headerRight.add(new Paragraph(vendorGstRegistered ? "TAX INVOICE" : "BILL OF SUPPLY")
	             .setFont(bold).setFontSize(18).setFontColor(brand));
	     headerRight.add(new Paragraph("Invoice No: " + invoiceNumber).setFont(normal).setFontSize(9));
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

	     // Place of supply: the pickup location's state when it could be determined (that's the
	     // actual GST place-of-supply for an unregistered/individual recipient — see the interState
	     // comment above), falling back to the vendor's own registered state, then their address,
	     // rather than leaving the field blank.
	     String placeOfSupply = pickupState != null ? pickupState : (vendorState != null ? vendorState : vendorAddress);

	     Table invoiceInfo = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
	     invoiceInfo.setWidth(UnitValue.createPercentValue(100));
	     invoiceInfo.addCell(plainCell("Pickup Date: " + safeText(transfer.getPickupDate() != null ? transfer.getPickupDate().toString() : null), normal));
	     invoiceInfo.addCell(plainCell("Pickup Slot: " + safeText(transfer.getPickupSchedule()), normal));
	     invoiceInfo.addCell(plainCell("Place of Supply: " + safeText(placeOfSupply), normal));
	     if (vendorGstRegistered) {
	         invoiceInfo.addCell(plainCell("Reverse Charge Applicable: No", normal));
	     }
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

	     // TODO: SAC 9965 (Goods Transport by Road) is used for every service type below — worth
	     // confirming with an accountant whether HOMESHIFTING (packers-and-movers-style household
	     // goods relocation) should actually use a different SAC code; not changed here since an
	     // incorrect substitute would be worse than the current single-code default.
	     String primaryChargeDescription;
	     if (transfer.getServiceType() == serviceTypeEnum.BOOKVEHICLE) {
	         primaryChargeDescription = "Vehicle Booking Charges";
	     } else if (transfer.getServiceType() == serviceTypeEnum.HOMESHIFTING) {
	         primaryChargeDescription = "Home Shifting Charges";
	     } else if (transfer.getServiceType() == serviceTypeEnum.TRANSFERSERVICE) {
	         primaryChargeDescription = "Parcel / Vehicle Transfer Charges";
	     } else {
	         primaryChargeDescription = "Ride Charges";
	     }

	     Table table = new Table(UnitValue.createPercentArray(new float[]{6, 40, 12, 18}));
	     table.setWidth(UnitValue.createPercentValue(100));

	     table.addHeaderCell(headerCell("S.No", bold, brand));
	     table.addHeaderCell(headerCell("Description", bold, brand));
	     table.addHeaderCell(headerCell("SAC", bold, brand));
	     table.addHeaderCell(headerCell("Amount (Rs.)", bold, brand));

	     int sno = 1;
	     table.addCell(dataCell(String.valueOf(sno++), normal, TextAlignment.CENTER));
	     table.addCell(dataCell(primaryChargeDescription, normal, TextAlignment.LEFT));
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

	     if (vendorGstRegistered) {
	         totals.addCell(totalsCell("Taxable Value", normal, false));
	         totals.addCell(totalsCell(money.format(taxableValue), normal, false));

	         if (interState) {
	             totals.addCell(totalsCell("IGST @ 18%", normal, false));
	             totals.addCell(totalsCell(money.format(totalGst), normal, false));
	         } else {
	             totals.addCell(totalsCell("CGST @ 9%", normal, false));
	             totals.addCell(totalsCell(money.format(cgst), normal, false));

	             totals.addCell(totalsCell("SGST @ 9%", normal, false));
	             totals.addCell(totalsCell(money.format(sgst), normal, false));
	         }
	     } else {
	         // Bill of Supply — an unregistered supplier can't legally charge GST, so no tax
	         // breakup is shown at all, just the total value of the supply.
	         totals.addCell(totalsCell("Total Value", normal, false));
	         totals.addCell(totalsCell(money.format(grandTotal), normal, false));
	     }

	     totals.addCell(totalsCell("Grand Total", bold, true));
	     totals.addCell(totalsCell(money.format(grandTotal), bold, true));

	     document.add(totals);

	     document.add(new Paragraph("\n" + GstInvoiceUtil.amountInWords(grandTotal))
	             .setFont(bold).setFontSize(9));

	     if (vendorGstRegistered) {
	         String taxNote = interState
	                 ? "Note: Inter-state supply (" + safeText(vendorState) + " to " + safeText(pickupState) + ") — IGST charged."
	                 : "Note: Intra-state supply — CGST + SGST charged.";
	         document.add(new Paragraph("\n" + taxNote)
	                 .setFont(normal).setFontSize(8).setFontColor(ColorConstants.GRAY));
	     } else {
	         document.add(new Paragraph("\nThis is a Bill of Supply. No GST has been charged as the supplier is not registered under GST.")
	                 .setFont(normal).setFontSize(8).setFontColor(ColorConstants.GRAY));
	     }

	     document.add(new Paragraph("\nDeclaration: We certify that the particulars given above are true and correct.")
	             .setFont(normal).setFontSize(8).setFontColor(ColorConstants.DARK_GRAY));

	     document.add(new Paragraph("\n\n"));

	     //================ FOOTER =================

	     Paragraph sign = new Paragraph("For " + safeText(vendorName))
	             .setFont(bold)
	             .setTextAlignment(TextAlignment.RIGHT);

	     document.add(sign);

	     if (signatureBytes != null) {
	         Image signatureImage = new Image(ImageDataFactory.create(signatureBytes))
	                 .setWidth(120)
	                 .setHeight(50)
	                 .setHorizontalAlignment(HorizontalAlignment.RIGHT);
	         document.add(signatureImage);
	     } else {
	         document.add(new Paragraph("\n\n"));
	     }

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
		RazorpayClient razorpayClient = paymentService.getPaymentClient();
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
	    	  logger.error("Ride cost calculation failed: {}", e.getMessage(), e);
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
	            paymentService.getPaymentClient();

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

	    // Idempotency — see the identical check/comment in /wallet/payment-success. Here a replay
	    // would re-activate the free period and insert a duplicate log row rather than double-credit
	    // money, but it's still worth rejecting cleanly rather than silently re-running.
	    if (walletTransactionRepository.existsByRazorpayPaymentId(req.getRazorpayPaymentId())) {
	        return ResponseEntity.ok("Subscription Activated");
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
	    walletTransaction.setRazorpayOrderId(req.getRazorpayOrderId());
	    walletTransaction.setRazorpayPaymentId(req.getRazorpayPaymentId());

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
	            paymentService.getPaymentClient();

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

	    // Idempotency — see the identical check/comment in /wallet/payment-success. Without this, a
	    // replayed request would re-activate (harmless-ish, already-paying) but also insert another
	    // duplicate "Subscription Purchased" log row every time it's replayed.
	    if (walletTransactionRepository.existsByRazorpayPaymentId(req.getRazorpayPaymentId())) {
	        return ResponseEntity.ok("Subscription Activated");
	    }

	    LocalDate localDate = LocalDate.now();
	    String plan = req.getPlan();

	    // The signature only proves SOME real payment happened for this razorpayOrderId — it says
	    // nothing about which plan that payment was actually for. Without this check, a vendor
	    // could pay for the cheapest plan, then submit that genuinely-valid signature here with
	    // plan="TWELVE_MONTH" and get 12 months activated for the price of 1. Re-fetching the
	    // order's real amount and checking it against what this plan should cost closes that gap —
	    // same pattern /wallet/payment-success already uses for its own amount.
	    RazorpayClient client = paymentService.getPaymentClient();
	    Order order = client.orders.fetch(req.getRazorpayOrderId());
	    Integer orderAmountPaise = order.get("amount");
	    if (orderAmountPaise == null || orderAmountPaise != amountForPlan(plan)) {
	        logger.warn("Subscription payment amount mismatch for vendor {}: order {} paid {} paise, plan {} costs {} paise",
	                req.getVendorId(), req.getRazorpayOrderId(), orderAmountPaise, plan, amountForPlan(plan));
	        return ResponseEntity.badRequest().body("Paid amount does not match the selected plan");
	    }

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
	    walletTransaction.setRazorpayOrderId(req.getRazorpayOrderId());
	    walletTransaction.setRazorpayPaymentId(req.getRazorpayPaymentId());
	    walletTransactionRepository.save(walletTransaction);

	    return ResponseEntity.ok(
	            "Subscription Activated");
	}
	
	
	@PostMapping("/wallet/create-order")
	public Map<String,Object> createOrder(
	        @RequestParam Long vendorId,
	        @RequestParam Double amount) throws Exception {

	    RazorpayClient client =
	        paymentService.getPaymentClient();

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
	@Transactional
	public ResponseEntity<?> paymentSuccess(
	        @RequestBody WalletPaymentRequest request,
	        HttpServletRequest httpRequest)
	        throws Exception {

	    // Same signature check the subscription endpoints already use — without it, this endpoint
	    // would credit a wallet for a "payment" that was never actually made against Razorpay.
	    if (!isValidPaymentSignature(request.getRazorpayOrderId(), request.getRazorpayPaymentId(), request.getRazorpaySignature())) {
	        return ResponseEntity.badRequest().body("Invalid Signature");
	    }

	    // The caller's own JWT must be the vendor being credited — otherwise any authenticated
	    // vendor/driver/customer could top up someone else's wallet using their own genuine payment.
	    String authHeader = httpRequest.getHeader("Authorization");
	    String jwt = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
	    Long tokenVendorId = jwt != null ? tokenApi.extractUserId(jwt) : null;
	    if (tokenVendorId == null || !tokenVendorId.equals(request.getVendorId())) {
	        throw new AccessDeniedException("You are not authorized to credit this vendor's wallet");
	    }

	    // Idempotency: Razorpay issues one payment id per actual charge, so a second request
	    // carrying the same one is necessarily a retry/replay of a request already processed
	    // (client timeout-then-retry, a double-tap, or a captured request replayed) — not a second
	    // real payment. Without this check, re-processing it would credit the wallet again for a
	    // single real charge. Checked before the Razorpay call below so a known replay doesn't even
	    // cost a network round-trip.
	    if (walletTransactionRepository.existsByRazorpayPaymentId(request.getRazorpayPaymentId())) {
	        return ResponseEntity.ok("SUCCESS");
	    }

	    // The signature only proves razorpayOrderId/razorpayPaymentId are a genuine matched pair —
	    // it says nothing about the amount, so a client could still claim any amount it likes for a
	    // real payment. Re-fetching the order from Razorpay and crediting *that* amount (not the
	    // client-supplied one) closes that gap.
	    RazorpayClient client = paymentService.getPaymentClient();
	    Order order = client.orders.fetch(request.getRazorpayOrderId());
	    Integer orderAmountPaise = order.get("amount");
	    if (orderAmountPaise == null) {
	        return ResponseEntity.badRequest().body("Could not verify order amount");
	    }
	    double verifiedAmount = orderAmountPaise / 100.0;

	    VendorWallet wallet =
	            VendorWalletRepo.findByVendor(
	                    request.getVendorId());

	    Optional<TransferVendor> vendor=transferVendorRepo.findById(request.getVendorId());
	    if (vendor.isEmpty()) {
	        throw new ResourceNotFoundException("Vendor not found with id: " + request.getVendorId());
	    }

	    if (wallet == null) {
	    	 wallet = new VendorWallet();
	    	 wallet.setBalance(verifiedAmount);
	    }else {

	    wallet.setBalance(
	            wallet.getBalance()
	            + verifiedAmount);
	    }
	    wallet.setVendor(vendor.get());
	    VendorWalletRepo.save(wallet);

	    WalletTransaction txn =
	            new WalletTransaction();

	    txn.setAmount(verifiedAmount);
	    txn.setTransactionType("CREDIT");
	    txn.setDescription(
	            "Wallet Recharge");

	    txn.setVendor(wallet.getVendor());
	    txn.setRazorpayOrderId(request.getRazorpayOrderId());
	    txn.setRazorpayPaymentId(request.getRazorpayPaymentId());

	    walletTransactionRepository.save(txn);

	    return ResponseEntity.ok("SUCCESS");
	}

	// Safety net for "Razorpay charged the customer, but our server never got to record it" —
	// e.g. the app is killed or loses its connection the instant after paying, before it can call
	// /wallet/payment-success or /subscription/verifyPayment itself. Razorpay calls this
	// independently, server-to-server, the moment it captures a payment, regardless of whether the
	// client-side confirmation call ever happens. Whichever path (this webhook, or the client's own
	// confirmation call) runs first does the real work; the existsByRazorpayPaymentId check below
	// means the other is always a safe no-op — so configuring this does not create a double-credit
	// risk on top of the normal flow.
	//
	// Requires one-time setup on your end, which I can't do myself: create a webhook in the
	// Razorpay dashboard (Settings -> Webhooks) pointing at this endpoint's full URL, subscribed to
	// the "payment.captured" event, then set RAZORPAY_WEBHOOK_SECRET on Railway to the secret
	// Razorpay generates for it. Until that's done, pay.webhook.secret is blank and every request
	// here is rejected (see isValidWebhookSignature) — safe to deploy before that setup is done.
	//
	// @RequestBody String (not a parsed DTO): signature verification needs the exact raw bytes
	// Razorpay signed — re-serializing a parsed object could produce different whitespace/key
	// ordering and make a genuine webhook fail verification.
	@PostMapping("/webhook/razorpay")
	public ResponseEntity<String> razorpayWebhook(
	        @RequestBody String rawPayload,
	        @RequestHeader(value = "X-Razorpay-Signature", required = false) String signature) {

	    if (!isValidWebhookSignature(rawPayload, signature)) {
	        logger.warn("Rejected /pay/webhook/razorpay call with invalid or missing signature");
	        return ResponseEntity.status(400).body("Invalid signature");
	    }

	    try {
	        JSONObject event = new JSONObject(rawPayload);
	        String eventType = event.optString("event", "");

	        if ("refund.processed".equals(eventType)) {
	            return handleRefundProcessed(event);
	        }

	        if ("payment.failed".equals(eventType)) {
	            return handlePaymentFailed(event);
	        }

	        // Only this event actually means "money has been captured" — webhooks fire for many
	        // other event types (order.paid, refund.created, ...), and acting on anything but a
	        // genuinely captured payment here would risk crediting for a payment that didn't
	        // actually succeed.
	        if (!"payment.captured".equals(eventType)) {
	            return ResponseEntity.ok("Ignored event: " + eventType);
	        }

	        JSONObject paymentEntity = event.getJSONObject("payload")
	                .getJSONObject("payment")
	                .getJSONObject("entity");

	        String paymentId = paymentEntity.getString("id");
	        String orderId = paymentEntity.optString("order_id", null);

	        // Same idempotency guard as every client-facing payment endpoint — if the client's own
	        // confirmation call already processed this payment (the common case; this webhook is
	        // only meant to catch the cases where it didn't), this is a no-op. Also covers Razorpay
	        // redelivering the same webhook more than once, which it does by design.
	        if (walletTransactionRepository.existsByRazorpayPaymentId(paymentId)) {
	            return ResponseEntity.ok("Already processed");
	        }

	        if (orderId == null) {
	            logger.warn("payment.captured webhook for payment {} has no order_id — cannot reconcile", paymentId);
	            return ResponseEntity.ok("No order_id, nothing to reconcile");
	        }

	        // The payment entity doesn't carry the order's receipt — fetch the order itself, the
	        // same call every other payment endpoint in this class already makes, to get the
	        // authoritative receipt (which vendor, which kind of purchase) and amount.
	        RazorpayClient client = paymentService.getPaymentClient();
	        Order order = client.orders.fetch(orderId);
	        String receipt = order.get("receipt");
	        Integer amountPaise = order.get("amount");

	        if (receipt == null || amountPaise == null) {
	            logger.warn("payment.captured webhook for payment {} / order {}: missing receipt or amount, cannot reconcile",
	                    paymentId, orderId);
	            return ResponseEntity.ok("Missing receipt/amount, nothing to reconcile");
	        }

	        if (receipt.startsWith("wallet_")) {
	            reconcileWalletRecharge(receipt, orderId, paymentId, amountPaise);
	        } else if (receipt.startsWith("subscription_")) {
	            reconcileSubscriptionPurchase(receipt, orderId, paymentId, amountPaise);
	        } else {
	            logger.warn("payment.captured webhook for payment {} / order {}: unrecognised receipt '{}'",
	                    paymentId, orderId, receipt);
	        }

	        return ResponseEntity.ok("Processed");
	    } catch (Exception e) {
	        // Returning 200 even on an unexpected failure here is deliberate: Razorpay retries a
	        // webhook on non-2xx responses, and a bug in this handler shouldn't turn into Razorpay
	        // hammering this endpoint repeatedly. The exception is still logged for investigation —
	        // this is a safety net on top of the normal flow, not the only place this payment could
	        // still get reconciled (manually, or via the client's own confirmation call if it
	        // eventually does go through).
	        logger.error("Error processing Razorpay webhook: {}", e.getMessage(), e);
	        return ResponseEntity.ok("Error logged");
	    }
	}

	// The third scenario: money was taken AND correctly credited at the time, but is later taken
	// back — Razorpay (or the merchant, via the Razorpay dashboard) issues a refund, or a
	// chargeback is upheld. Without this, a reversed payment would leave the vendor permanently
	// keeping a wallet credit (or subscription) for money they no longer actually paid.
	//
	// Wallet recharges are reversed automatically — deducting a refunded amount is unambiguous,
	// even if it pushes the balance negative (that's the correct reflection of reality: the money
	// really is gone). Subscription purchases are NOT auto-reverted — unwinding "this vendor has
	// already been operating as ACTIVE for some number of days" is a business judgment call (do
	// they keep access for days already used? does a later legitimate renewal's dates get
	// affected?) that shouldn't be guessed at automatically. Those are logged at warning level
	// instead, for manual review, with the refund still recorded for the audit trail.
	private ResponseEntity<String> handleRefundProcessed(JSONObject event) {
	    JSONObject refundEntity = event.getJSONObject("payload")
	            .getJSONObject("refund")
	            .getJSONObject("entity");

	    String refundId = refundEntity.getString("id");
	    String paymentId = refundEntity.optString("payment_id", null);
	    int refundedAmountPaise = refundEntity.getInt("amount");

	    if (walletTransactionRepository.existsByRazorpayRefundId(refundId)) {
	        return ResponseEntity.ok("Refund already processed");
	    }
	    if (paymentId == null) {
	        logger.warn("refund.processed webhook {} has no payment_id — cannot reconcile", refundId);
	        return ResponseEntity.ok("No payment_id, nothing to reconcile");
	    }

	    Optional<WalletTransaction> original =
	            walletTransactionRepository.findFirstByRazorpayPaymentIdOrderByIdAsc(paymentId);
	    if (original.isEmpty()) {
	        logger.warn("refund.processed webhook {} for payment {}: no matching transaction on file, cannot reconcile",
	                refundId, paymentId);
	        return ResponseEntity.ok("No matching transaction, nothing to reconcile");
	    }

	    applyReversal(original.get(), refundedAmountPaise / 100.0, paymentId, refundId, "Refund processed");
	    return ResponseEntity.ok("Refund processed");
	}

	// The fourth, related scenario: a payment that LOOKED successful and was credited at the time,
	// but the bank itself later fails/reverses it — e.g. a UPI or netbanking settlement that
	// doesn't actually go through, which Razorpay can only find out asynchronously, after already
	// reporting the payment as captured. This is NOT a merchant-initiated refund (no refund id
	// exists for it) — Razorpay simply transitions the same payment to "failed" and fires this
	// event. The ordinary case for payment.failed is just "a checkout attempt that never succeeded
	// in the first place" — nothing was ever credited for those, so there's nothing to reverse;
	// this only does something when we find a transaction already on file for this exact payment.
	private ResponseEntity<String> handlePaymentFailed(JSONObject event) {
	    JSONObject paymentEntity = event.getJSONObject("payload")
	            .getJSONObject("payment")
	            .getJSONObject("entity");

	    String paymentId = paymentEntity.getString("id");

	    Optional<WalletTransaction> original =
	            walletTransactionRepository.findFirstByRazorpayPaymentIdOrderByIdAsc(paymentId);
	    if (original.isEmpty()) {
	        // The normal case — this payment never got far enough to be credited for in the first
	        // place. Not logged at warning level; this is the expected outcome for most failed
	        // checkout attempts, not something that needs attention.
	        return ResponseEntity.ok("Payment failed before anything was credited — no action needed");
	    }

	    // No refund id exists for this kind of reversal, so idempotency is keyed on "has this
	    // payment id already got a second (reversal) row" instead of a distinct reference id.
	    if (walletTransactionRepository.countByRazorpayPaymentId(paymentId) > 1) {
	        return ResponseEntity.ok("Already reversed");
	    }

	    int amountPaise = paymentEntity.optInt("amount", 0);
	    applyReversal(original.get(), amountPaise / 100.0, paymentId, null,
	            "Bank-side failure after apparent capture");
	    return ResponseEntity.ok("Reversed a previously-credited payment that the bank later failed");
	}

	// Shared by handleRefundProcessed and handlePaymentFailed — both need to undo whatever
	// `original` did, just for different reasons (a deliberate refund vs. a late bank failure) and
	// with different reference ids available (a refund has its own id; a bank-side failure doesn't).
	//
	// Wallet recharges are reversed automatically — deducting the amount back out is unambiguous,
	// even if it pushes the balance negative (that's the correct reflection of reality: the money
	// really is gone). Subscription purchases are NOT auto-reverted — unwinding "this vendor has
	// already been operating as ACTIVE for some number of days" is a business judgment call (do
	// they keep access for days already used? does a later legitimate renewal's dates get
	// affected?) that shouldn't be guessed at automatically. Those are logged at warning level
	// instead, for manual review, with the reversal still recorded for the audit trail.
	private void applyReversal(WalletTransaction original, double amount, String paymentId,
	        String refundId, String reasonLabel) {
	    TransferVendor vendor = original.getVendor();
	    boolean isWalletRecharge = "CREDIT".equals(original.getTransactionType());

	    WalletTransaction reversal = new WalletTransaction();
	    reversal.setVendor(vendor);
	    reversal.setAmount(amount);
	    reversal.setRazorpayPaymentId(paymentId);
	    reversal.setRazorpayRefundId(refundId);

	    if (isWalletRecharge) {
	        VendorWallet wallet = VendorWalletRepo.findByVendor(vendor.getId());
	        if (wallet != null) {
	            wallet.setBalance(wallet.getBalance() - amount);
	            VendorWalletRepo.save(wallet);
	        }
	        reversal.setTransactionType("DEBIT");
	        reversal.setDescription("Wallet Recharge Reverted — " + reasonLabel);
	        walletTransactionRepository.save(reversal);
	        logger.warn("{}: reversed {} from vendor {}'s wallet for payment {}",
	                reasonLabel, amount, vendor.getId(), paymentId);
	    } else {
	        reversal.setTransactionType("Reverted - Subscription (manual review required)");
	        reversal.setDescription("Original purchase: " + original.getTransactionType()
	                + " — " + reasonLabel + " — subscription/vendor status NOT automatically changed, review manually");
	        walletTransactionRepository.save(reversal);
	        logger.warn("{} for a SUBSCRIPTION payment: vendor {}, amount {}, payment {} — "
	                + "subscription/vendor status was NOT automatically changed, needs manual review",
	                reasonLabel, vendor.getId(), amount, paymentId);
	    }
	}

	// Manual "fix this one payment" tool for whenever the automatic safety nets above didn't
	// (already-happened cases from before the webhook was configured, a webhook delivery that
	// failed for some reason, or just wanting to double-check a specific payment on request).
	// Usage: find the payment id on the Razorpay dashboard (Payments list) for whatever the vendor
	// is disputing, then call:
	//   POST /pay/admin/reconcile-payment?paymentId=pay_XXXXXXXXXXXX
	//   Header: X-Admin-Key: <the ADMIN_RECONCILE_KEY value>
	// It independently re-checks the payment's actual status with Razorpay (not just trusting the
	// caller), so this can't be used to credit something that was never really paid. Safe to call
	// on an already-processed payment — it'll just report that and do nothing.
	@PostMapping("/admin/reconcile-payment")
	public ResponseEntity<String> reconcilePayment(
	        @RequestParam String paymentId,
	        @RequestHeader(value = "X-Admin-Key", required = false) String adminKey) throws RazorpayException {

	    if (adminReconcileKey == null || adminReconcileKey.isBlank()
	            || adminKey == null || !adminReconcileKey.equals(adminKey)) {
	        throw new AccessDeniedException("Not authorized to use this endpoint");
	    }

	    if (walletTransactionRepository.existsByRazorpayPaymentId(paymentId)) {
	        return ResponseEntity.ok("Already recorded — nothing to do. Check the wallet_transaction "
	                + "table for razorpay_payment_id = " + paymentId + " to see the existing entry.");
	    }

	    RazorpayClient client = paymentService.getPaymentClient();
	    Payment payment = client.payments.fetch(paymentId);
	    String status = payment.get("status");
	    if (!"captured".equals(status)) {
	        return ResponseEntity.badRequest().body("Payment " + paymentId + " has status '" + status
	                + "', not 'captured' — refusing to credit anything for a payment that wasn't actually captured.");
	    }

	    String orderId = payment.get("order_id");
	    if (orderId == null) {
	        return ResponseEntity.badRequest().body("Payment " + paymentId + " has no order_id — cannot determine "
	                + "which vendor/purchase this was for.");
	    }

	    Order order = client.orders.fetch(orderId);
	    String receipt = order.get("receipt");
	    Integer amountPaise = order.get("amount");
	    if (receipt == null || amountPaise == null) {
	        return ResponseEntity.badRequest().body("Order " + orderId + " is missing receipt or amount — cannot reconcile.");
	    }

	    if (receipt.startsWith("wallet_")) {
	        reconcileWalletRecharge(receipt, orderId, paymentId, amountPaise);
	        return ResponseEntity.ok("Wallet recharge reconciled for payment " + paymentId + " (order " + orderId + ")");
	    } else if (receipt.startsWith("subscription_")) {
	        reconcileSubscriptionPurchase(receipt, orderId, paymentId, amountPaise);
	        return ResponseEntity.ok("Subscription purchase reconciled for payment " + paymentId + " (order " + orderId + ")");
	    }
	    return ResponseEntity.badRequest().body("Unrecognised receipt '" + receipt + "' on order " + orderId);
	}

	private boolean isValidWebhookSignature(String rawPayload, String signature) {
	    String webhookSecret = paymentService.getWebhookSecret();
	    if (webhookSecret == null || webhookSecret.isBlank() || signature == null) {
	        return false;
	    }
	    try {
	        return com.razorpay.Utils.verifyWebhookSignature(rawPayload, signature, webhookSecret);
	    } catch (Exception e) {
	        logger.warn("Webhook signature verification failed: {}", e.getMessage());
	        return false;
	    }
	}

	// receipt is "wallet_<vendorId>" (see /wallet/create-order) — same crediting logic as
	// /wallet/payment-success's happy path, just triggered independently by the webhook instead of
	// the client's own confirmation call.
	private void reconcileWalletRecharge(String receipt, String orderId, String paymentId, int amountPaise) {
	    Long vendorId = Long.valueOf(receipt.substring("wallet_".length()));
	    double amount = amountPaise / 100.0;

	    Optional<TransferVendor> vendor = transferVendorRepo.findById(vendorId);
	    if (vendor.isEmpty()) {
	        logger.warn("Webhook reconciliation: vendor {} not found for wallet recharge, payment {}", vendorId, paymentId);
	        return;
	    }

	    VendorWallet wallet = VendorWalletRepo.findByVendor(vendorId);
	    if (wallet == null) {
	        wallet = new VendorWallet();
	        wallet.setBalance(amount);
	    } else {
	        wallet.setBalance(wallet.getBalance() + amount);
	    }
	    wallet.setVendor(vendor.get());
	    VendorWalletRepo.save(wallet);

	    WalletTransaction txn = new WalletTransaction();
	    txn.setAmount(amount);
	    txn.setTransactionType("CREDIT");
	    txn.setDescription("Wallet Recharge (reconciled via webhook)");
	    txn.setVendor(wallet.getVendor());
	    txn.setRazorpayOrderId(orderId);
	    txn.setRazorpayPaymentId(paymentId);
	    walletTransactionRepository.save(txn);

	    logger.warn("Webhook reconciled a wallet recharge the client-side confirmation call apparently missed: "
	            + "vendor {}, amount {}, payment {}", vendorId, amount, paymentId);
	}

	// receipt is "subscription_<vendorId>" (see /subscription/createOrder and /subscription/renew
	// — both use the same receipt format, so this can't distinguish a first purchase from a
	// renewal from the receipt alone). Always applies the renewal-style start-date rule (extend
	// from the later of today or the existing end date) rather than always resetting to today —
	// safe either way: if there's no existing/active subscription that's exactly equivalent to
	// starting from today, and if there IS still time left on an active one, extending it is the
	// correct behavior regardless of which of the two flows actually triggered this payment.
	private void reconcileSubscriptionPurchase(String receipt, String orderId, String paymentId, int amountPaise) {
	    Long vendorId = Long.valueOf(receipt.substring("subscription_".length()));
	    String plan = planForAmount(amountPaise);
	    if (plan == null) {
	        logger.warn("Webhook reconciliation: payment {} / order {} amount {} paise doesn't match any known plan price",
	                paymentId, orderId, amountPaise);
	        return;
	    }

	    Optional<TransferVendor> vendor = transferVendorRepo.findById(vendorId);
	    if (vendor.isEmpty()) {
	        logger.warn("Webhook reconciliation: vendor {} not found for subscription purchase, payment {}", vendorId, paymentId);
	        return;
	    }

	    LocalDate today = LocalDate.now();
	    Subscription subscription = paymentRepo.findByVendorId(vendorId);
	    if (subscription == null) {
	        subscription = new Subscription();
	        subscription.setVendor(vendor.get());
	    }
	    LocalDate startDate = (subscription.getEndDate() != null && subscription.getEndDate().plusDays(1).isAfter(today))
	            ? subscription.getEndDate().plusDays(1)
	            : today;

	    applyPlanToSubscription(subscription, plan, startDate);
	    paymentRepo.save(subscription);
	    transferVendorRepo.activateVendor(vendorId, 3);

	    WalletTransaction txn = new WalletTransaction();
	    txn.setAmount(amountPaise / 100.0);
	    txn.setVendor(vendor.get());
	    txn.setTransactionType("Subscription Purchased (reconciled via webhook)");
	    txn.setRazorpayOrderId(orderId);
	    txn.setRazorpayPaymentId(paymentId);
	    walletTransactionRepository.save(txn);

	    logger.warn("Webhook reconciled a subscription purchase the client-side confirmation call apparently missed: "
	            + "vendor {}, plan {}, payment {}", vendorId, plan, paymentId);
	}

	private String planForAmount(int amountPaise) {
	    if (amountPaise == amountForPlan("MONTHLY")) return "MONTHLY";
	    if (amountPaise == amountForPlan("THREE_MONTH")) return "THREE_MONTH";
	    if (amountPaise == amountForPlan("SIX_MONTH")) return "SIX_MONTH";
	    if (amountPaise == amountForPlan("TWELVE_MONTH")) return "TWELVE_MONTH";
	    return null;
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
		            paymentService.getPaymentClient();

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

	    // Idempotency — see the identical check/comment in /wallet/payment-success.
	    if (walletTransactionRepository.existsByRazorpayPaymentId(req.getRazorpayPaymentId())) {
	        return ResponseEntity.ok("Subscription Renewed");
	    }

	    // Amount-vs-plan check — see the identical check/comment in /subscription/verifyPayment.
	    RazorpayClient client = paymentService.getPaymentClient();
	    Order order = client.orders.fetch(req.getRazorpayOrderId());
	    Integer orderAmountPaise = order.get("amount");
	    String plan = req.getPlan();
	    if (orderAmountPaise == null || orderAmountPaise != amountForPlan(plan)) {
	        logger.warn("Subscription renewal amount mismatch for vendor {}: order {} paid {} paise, plan {} costs {} paise",
	                req.getVendorId(), req.getRazorpayOrderId(), orderAmountPaise, plan, amountForPlan(plan));
	        return ResponseEntity.badRequest().body("Paid amount does not match the selected plan");
	    }

	    LocalDate localDate = LocalDate.now();

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
	    walletTransaction.setRazorpayOrderId(req.getRazorpayOrderId());
	    walletTransaction.setRazorpayPaymentId(req.getRazorpayPaymentId());
	    walletTransactionRepository.save(walletTransaction);


	    return ResponseEntity.ok(
	            "Subscription Renewed");
	}
	
	
}