package com.samadhan.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInvoiceRequest {

    private String type;
    private String description;
    @JsonProperty(value = "partial_payment")
    private Boolean partialPayment;
    private Customer customer;
    @JsonProperty(value = "line_items")
    private List<LineItem> lineItems;
    @JsonProperty(value = "email_notify")
    private Integer emailNotify;
    @JsonProperty(value = "sms_notify")
    private Integer smsNotify;
	private String currency;
	@JsonProperty(value = "expire_by")
	private Long expireBy;
}
