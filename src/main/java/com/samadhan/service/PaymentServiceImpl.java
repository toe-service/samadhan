package com.samadhan.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.dto.RideCostSummary;
import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.SubscriptionPrice;
import com.samadhan.response.SubscriptionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
//        return Arrays.stream(SubscriptionPrice.values())
//                .map(obj -> new SubscriptionResponse(obj.getSubscriptionName(), obj.getPrice(), Collections.emptyList()))
//                .collect(Collectors.toList());
    	return null;
    }

	public RideCostSummary getrideCostCalculation(String pickuplatitude, String pickuplongitude,
			String destinationlatitude, String destinationlongitude, ParcelTypeEnum parcelType, CarModelEnum carModel,
			BikeModelEnum bikeModel, Double parcelWeight) {

		String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickuplatitude + ","
				+ pickuplongitude + "&destination=" + destinationlatitude + "," + destinationlongitude
				+ "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";

		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.getForObject(url, String.class);

		RideCostSummary rideSummary = new RideCostSummary();

		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response);

			int distanceInMeters = root.path("routes").get(0).path("legs").get(0).path("distance").path("value")
					.asInt();

			double distanceInKm = distanceInMeters / 1000.0;

			// ✅ Step 1: Get weight
			Double effectiveWeight = 0.0;

			if (parcelType != null && parcelType.getType().equalsIgnoreCase("Package")) {
				effectiveWeight = parcelWeight;
			} else if (carModel != null) {
				effectiveWeight = carModel.getAverageWeightKg();
			} else if (bikeModel != null) {
				effectiveWeight = bikeModel.getAverageWeightKg();
			}

			// ✅ Step 2: Weight factor
			Double weightFactor = 1.0;

			if (effectiveWeight <= 5) {
				weightFactor = 1.0;
			} else if (effectiveWeight <= 20) {
				weightFactor = 1.2;
			} else if (effectiveWeight <= 100) {
				weightFactor = 1.5;
			} else if (effectiveWeight <= 500) {
				weightFactor = 2.0;
			} else {
				weightFactor = 2.5;
			}

			// ✅ Step 3: Distance pricing
			double perKmRate = (distanceInKm <= 25) ? 10 : 7;

			// ✅ Step 4: Final ride cost
			double rideCalculation = distanceInKm * perKmRate * weightFactor;
			double loadingUnloading =0.0;
			double packaging =0.0;
			
			if(distanceInKm >=100 && effectiveWeight>120) {

			 loadingUnloading = 500.0;
			 packaging = 500.0;
			
			}

			double gst = rideCalculation * 0.18;

			double totalCost = rideCalculation + gst + loadingUnloading + packaging;

			rideSummary.setRideCost(rideCalculation);
			rideSummary.setGst(gst);
			rideSummary.setLoadingUnloading(loadingUnloading);
			rideSummary.setPackaging(packaging);
			rideSummary.setTotalCost(totalCost);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return rideSummary;
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