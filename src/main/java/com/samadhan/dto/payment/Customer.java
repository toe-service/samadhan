package com.samadhan.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String name;
    private String contact;
    private String email;
    @JsonProperty(value = "billing_address")
    private CustomerAddress billingAddress;
    @JsonProperty(value = "shipping_address")
    private CustomerAddress shippingAddress;
}
