package com.samadhan.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.samadhan.enums.PaymentTypeEnum;
import com.samadhan.enums.SubscriptionPeriodEnum;

@Entity
@Table(name="payment")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="payment_type")
	private PaymentTypeEnum paymentType;
	
	@Column(name="subscription_period")
	private SubscriptionPeriodEnum subscriptionPeriod;
	
	@Column(name="start_date")
	private Date startDate;
	
	@Column(name="end_date")
	private Date endDate;

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

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	
}
