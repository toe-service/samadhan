package com.samadhan.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="cancelled_request")
public class CancelledRequest {
	
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne
	    @JoinColumn(name = "vendor_id")
	    private TransferVendor transferVendor;

	    @ManyToOne
	    @JoinColumn(name = "transfer_request_id")
	    private TransferRequestDetails transferRequest;

	    @Column(name = "cancellation_reason")
	    private String cancellationReason;
	    
	    @Column(name = "request_flag")
	    private Boolean requestFlag;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public TransferVendor getTransferVendor() {
			return transferVendor;
		}

		public void setTransferVendor(TransferVendor transferVendor) {
			this.transferVendor = transferVendor;
		}

		public TransferRequestDetails getTransferRequest() {
			return transferRequest;
		}

		public void setTransferRequest(TransferRequestDetails transferRequest) {
			this.transferRequest = transferRequest;
		}

		public String getcancellationReason() {
			return cancellationReason;
		}

		public void setcancellationReason(String cancellationReason) {
			this.cancellationReason = cancellationReason;
		}

		public String getCancellationReason() {
			return cancellationReason;
		}

		public void setCancellationReason(String cancellationReason) {
			this.cancellationReason = cancellationReason;
		}

		public Boolean getRequestFlag() {
			return requestFlag;
		}

		public void setRequestFlag(Boolean requestFlag) {
			this.requestFlag = requestFlag;
		}
	    
	    

}
