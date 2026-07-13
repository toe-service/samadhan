package com.samadhan.entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.samadhan.enums.PaymentTypeEnum;
import com.samadhan.enums.SubscriptionPeriodEnum;

@Entity
@Table(name="subscription")
public class Subscription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="payment_type")
	private PaymentTypeEnum paymentType;
	
	@Column(name="subscription_period")
	private SubscriptionPeriodEnum subscriptionPeriod;
	
	@Column(name="start_date")
	private LocalDate startDate;
	
	@Column(name="end_date")
	private LocalDate endDate;
	
//	@OneToOne(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private ServiceCentre serviceCentre;
	
	@ManyToOne
	@JoinColumn(name = "service_centre_id")
	private ServiceCentre serviceCentre;
	
	@OneToOne
	@JoinColumn(name = "vendor_id")
	@JsonBackReference
	private TransferVendor vendor;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public PaymentTypeEnum getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentTypeEnum paymentType) {
		this.paymentType = paymentType;
	}

	public SubscriptionPeriodEnum getSubscriptionPeriod() {
		return subscriptionPeriod;
	}

	public void setSubscriptionPeriod(SubscriptionPeriodEnum subscriptionPeriod) {
		this.subscriptionPeriod = subscriptionPeriod;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public ServiceCentre getServiceCentre() {
		return serviceCentre;
	}

	public void setServiceCentre(ServiceCentre serviceCentre) {
		this.serviceCentre = serviceCentre;
	}

	public TransferVendor getVendor() {
		return vendor;
	}

	public void setVendor(TransferVendor vendor) {
		this.vendor = vendor;
	}
	
	
}
