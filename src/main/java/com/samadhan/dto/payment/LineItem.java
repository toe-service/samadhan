package com.samadhan.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LineItem {

    private String name;
    private String description;
    private Integer amount;
    private String currency;
    private Integer quantity;
}
