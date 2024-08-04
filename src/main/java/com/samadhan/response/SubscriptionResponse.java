package com.samadhan.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private String name;
    private Double price;
    private List<String> description;
}
