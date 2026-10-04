package com.samadhan.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double amount;

    private String transactionType;

    private String description;

    private Long referenceId;
    
    @Column(name="createdDate")
    private LocalDate createdDate;

    // Precise timestamp, as opposed to createdDate's date-only precision — kept as a separate new
    // column rather than widening createdDate so existing date-only queries/display keep working
    // unchanged.
    @Column(name = "transaction_time")
    private LocalDateTime transactionTime;

    // Only populated for Razorpay-driven entries (wallet recharge, subscription purchase/renewal)
    // — null for internal wallet debits (ride acceptance/start/completion fees) that never touch
    // Razorpay. razorpayPaymentId is what makes a credit idempotent: Razorpay issues one
    // payment id per actual charge, so checking for an existing row with the same id before
    // crediting again is what stops the same successful payment from being processed twice (a
    // client retry, a double-tap, or a replayed request all present the same payment id).
    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    // Only set on a reversal row (transactionType "Refund"/"Chargeback") — the refund's own id
    // (Razorpay issues "rfnd_..." ids distinct from the original "pay_..." payment id), so a
    // redelivered refund.processed webhook can be recognized as already-handled the same way
    // razorpayPaymentId already does for the original payment.
    @Column(name = "razorpay_refund_id")
    private String razorpayRefundId;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private TransferVendor vendor;
    
    @ManyToOne
    @JoinColumn(name = "transfer_request_id")
    private TransferRequestDetails transferRequestDetail;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Long getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(Long referenceId) {
		this.referenceId = referenceId;
	}

	public TransferVendor getVendor() {
		return vendor;
	}

	public void setVendor(TransferVendor vendor) {
		this.vendor = vendor;
	}

	public TransferRequestDetails getTransferRequestDetail() {
		return transferRequestDetail;
	}

	public void setTransferRequestDetail(TransferRequestDetails transferRequestDetail) {
		this.transferRequestDetail = transferRequestDetail;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDateTime getTransactionTime() {
		return transactionTime;
	}

	public void setTransactionTime(LocalDateTime transactionTime) {
		this.transactionTime = transactionTime;
	}

	public String getRazorpayOrderId() {
		return razorpayOrderId;
	}

	public void setRazorpayOrderId(String razorpayOrderId) {
		this.razorpayOrderId = razorpayOrderId;
	}

	public String getRazorpayPaymentId() {
		return razorpayPaymentId;
	}

	public void setRazorpayPaymentId(String razorpayPaymentId) {
		this.razorpayPaymentId = razorpayPaymentId;
	}

	public String getRazorpayRefundId() {
		return razorpayRefundId;
	}

	public void setRazorpayRefundId(String razorpayRefundId) {
		this.razorpayRefundId = razorpayRefundId;
	}

	@PrePersist
	public void prePersist() {
		if (createdDate == null) {
			createdDate = LocalDate.now();
		}
		if (transactionTime == null) {
			transactionTime = LocalDateTime.now();
		}
	}

}
