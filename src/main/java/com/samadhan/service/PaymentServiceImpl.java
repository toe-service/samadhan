package com.samadhan.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.enums.SubscriptionPrice;
import com.samadhan.response.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PaymentServiceImpl {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Value("${pay.key}")
    private String key;

    @Value("${pay.secret}")
    private String secret;

    public RazorpayClient getPaymentClient() throws RazorpayException {
        return new RazorpayClient(key,secret);
    }


//    public List<SubscriptionResponse> getAllSubscriptions() {
//        return Arrays.stream(SubscriptionPrice.values())
//                .map(obj -> new SubscriptionResponse(obj.getSubscriptionName(), obj.getPrice(), Collections.emptyList()))
//                .toList();
//    }
    
    public List<SubscriptionResponse> getAllSubscriptions() {
        return Arrays.stream(SubscriptionPrice.values())
                .map(obj -> new SubscriptionResponse(obj.getSubscriptionName(), obj.getPrice(), Collections.emptyList()))
                .collect(Collectors.toList());
    }

    public int getrideCostCalculation(int distance) {

//    double distance = calculateDistance(pickuplatitude, pickuplongitude, destinationlatitude, destinationlongitude);
    //double distance=3;
        int getrideCostCalculation=0;
    if(distance<5){
        getrideCostCalculation =1000;
    }else {
        getrideCostCalculation = distance * 200;
    }
    return getrideCostCalculation;

    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c; // Distance in kilometers
    }
}