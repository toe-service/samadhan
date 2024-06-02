package com.samadhan.util;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.response.GeneralResponse;
import com.samadhan.service.PaymentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class Utils {

    private static PaymentServiceImpl paymentService;

    public static GeneralResponse getSuccessResponse(String message) {
        return new GeneralResponse(message, 200, "");
    }

    public static GeneralResponse getUnauthorizeResponse() {
        return new GeneralResponse("Unauthorize", 401, "Unauthorize");
    }

    public static GeneralResponse getFailureResponse(Exception exp) {
        return new GeneralResponse("error", 401, exp.getMessage());
    }

    public static RazorpayClient getPaymentClient() throws RazorpayException {
        if(Objects.isNull(paymentService)) {
            paymentService = new PaymentServiceImpl();
        }
        return paymentService.getPaymentClient();
    }
}
