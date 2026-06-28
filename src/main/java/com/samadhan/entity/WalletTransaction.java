package com.samadhan.entity;

import lombok.Data;

import java.time.LocalDate;
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
	
	
    
}
