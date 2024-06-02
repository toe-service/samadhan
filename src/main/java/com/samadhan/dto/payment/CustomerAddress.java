package com.samadhan.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAddress {
    private String line1;
    private String line2;
    private String zipcode;
    private String state;
    private String city;
    private String country;
}
