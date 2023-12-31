package com.samadhan.response;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class GeneralResponse {
    public String message;
    public int statusCode;
    public String errorMessage;
}
