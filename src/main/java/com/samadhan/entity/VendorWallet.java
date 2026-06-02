package com.samadhan.entity;

import lombok.Data;

import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name="vendor_wallet")
public class VendorWallet {
	
	  	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Double balance;

	    private Double securityDeposit;

	    @OneToOne
	    @JoinColumn(name = "vendor_id")
	    private TransferVendor vendor;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Double getBalance() {
			return balance;
		}

		public void setBalance(Double balance) {
			this.balance = balance;
		}

		public Double getSecurityDeposit() {
			return securityDeposit;
		}

		public void setSecurityDeposit(Double securityDeposit) {
			this.securityDeposit = securityDeposit;
		}

		public TransferVendor getVendor() {
			return vendor;
		}

		public void setVendor(TransferVendor vendor) {
			this.vendor = vendor;
		}
	    

}
