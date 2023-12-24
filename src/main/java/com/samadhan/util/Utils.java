package com.samadhan.util;

import com.samadhan.response.GeneralResponse;
import org.springframework.stereotype.Component;

@Component
public class Utils {

    public GeneralResponse getSuccessResponse(String message) {
        return new GeneralResponse(message, 200, "");
    }

    public GeneralResponse getUnauthorizeResponse() {
        return new GeneralResponse("Unauthorize", 401, "Unauthorize");
    }

    public GeneralResponse getFailureResponse(Exception exp) {
        return new GeneralResponse("error", 401, exp.getMessage());
    }
}
