package com.samadhan.dto;

import java.time.LocalDate;

public class WalletTransactionDto {

	Long id;
	Double amount;
	String transactionType;
	String description;
	LocalDate createdDate;
	Long transferRequestId;

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
	public LocalDate getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}
	public Long getTransferRequestId() {
		return transferRequestId;
	}
	public void setTransferRequestId(Long transferRequestId) {
		this.transferRequestId = transferRequestId;
	}

}
