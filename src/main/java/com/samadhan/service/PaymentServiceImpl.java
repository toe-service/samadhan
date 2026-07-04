package com.samadhan.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.samadhan.dto.RideCostSummary;
import com.samadhan.entity.Subscription;
import com.samadhan.enums.BikeModelEnum;
import com.samadhan.enums.CarModelEnum;
import com.samadhan.enums.ParcelTypeEnum;
import com.samadhan.enums.SubscriptionPrice;
import com.samadhan.repository.PaymentRepository;
import com.samadhan.response.SubscriptionResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

@Component
public class PaymentServiceImpl {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Value("${pay.key}")
    private String key;

    @Value("${pay.secret}")
    private String secret;
    
    @Autowired
    PaymentRepository PaymentRepo;

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
			BikeModelEnum bikeModel, Double parcelWeight, String cc, Double length, Double width, Double heigth) throws JsonMappingException, JsonProcessingException {

		String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickuplatitude + ","
				+ pickuplongitude + "&destination=" + destinationlatitude + "," + destinationlongitude
				+ "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";

		RestTemplate restTemplate = new RestTemplate();
		String response = restTemplate.getForObject(url, String.class);

		RideCostSummary rideSummary = new RideCostSummary();

	//	try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response);

			int distanceInMeters = root.path("routes").get(0).path("legs").get(0).path("distance").path("value")
					.asInt();

			double distanceInKm = distanceInMeters / 1000.0;
			
//			if (distanceInKm < 50) {
//			    throw new RuntimeException(
//			        "Service is available only for distances greater than or equal to 50 KM"
//			    );
//			}
//			double volumeCubicInches = length * width * heigth;
//			
//			if(volumeCubicInches < 3000.0) {
//				throw new RuntimeException(
//				        "Service is available only for package size greater than or equal to 22 × 18 × 12 Inch"
//				    );
//			}
			
			double ccFactor = 1.0;

			if (bikeModel != null && cc != null && !cc.isEmpty()) {

				  int bikeCC = Integer.parseInt(cc);

			    if (bikeCC <= 100) {
			        ccFactor = 1.0;
			    }else if (bikeCC <= 199) {
			        ccFactor = 1.1;
			    }
			    else if (bikeCC <= 249) {
			        ccFactor = 1.2;
			    }
			    else if (bikeCC <= 349) {
			        ccFactor = 1.3;
			    }
			    else if (bikeCC <= 449) {
			        ccFactor = 1.4;
			    }
			    else if (bikeCC <= 599) {
			        ccFactor = 1.5;
			    }else if (bikeCC <= 799) {
			        ccFactor = 1.6;
			    }else if (bikeCC <= 999) {
			        ccFactor = 1.7;
			    } else {
			        ccFactor = 1.8;
			    }
			}
			
		

			if (carModel != null && cc != null && !cc.isEmpty()) {

			    int carCC = Integer.parseInt(cc);

			    if (carCC <= 799) {
			        ccFactor = 1.6;
			    }
			    else if (carCC <= 999) {
			        ccFactor = 1.7;
			    }
			    else if (carCC <= 1199) {
			        ccFactor = 1.8;
			    }
			    else if (carCC <= 1399) {
			        ccFactor = 1.9;
			    }
			    else if (carCC <= 1599) {
			        ccFactor = 2.0;
			    }
			    else if (carCC <= 1799) {
			        ccFactor = 2.1;
			    }
			    else if (carCC <= 2199) {
			        ccFactor = 2.2;
			    }
			    else if (carCC <= 2999) {
			        ccFactor = 2.3;
			    }
			    else {
			        ccFactor = 2.4;
			    }
			}
			

			// ✅ Step 1: Get weight
			Double effectiveWeight = 0.0;

			if (parcelType != null && parcelType.getType().equalsIgnoreCase("Package")) {
				effectiveWeight = parcelWeight;
			} else if (carModel != null) {
				effectiveWeight = carModel.getAverageWeightKg();
			} else if (bikeModel != null) {
				effectiveWeight = bikeModel.getAverageWeightKg();
			}
			Double weightFactor = 1.0;
			double sizeFactor = 1.0;
			if (parcelType != null && parcelType.getType().equalsIgnoreCase("Package")) {
				double volumeCubicInches = length * width * heigth;
				
				if(volumeCubicInches < 3000.0) {
					throw new RuntimeException(
					        "Service is available only for package size greater than or equal to 22 × 18 × 12 Inch"
					    );
				}
				

				if (volumeCubicInches <= 3000) {
				    sizeFactor = 1.0;
				} else if (volumeCubicInches <= 6000) {
				    sizeFactor = 1.2;
				} else if (volumeCubicInches <= 12000) {
				    sizeFactor = 1.40;
				} else if (volumeCubicInches <= 20000) {
				    sizeFactor = 1.50;
				} else {
				    sizeFactor = 1.75;
				}
				
				
			// ✅ Step 2: Weight factor
			
			if (effectiveWeight <= 5) {
				weightFactor = 1.0;
			} else if (effectiveWeight <= 20) {
				weightFactor = 1.1;
			} else if (effectiveWeight <= 50) {
				weightFactor = 1.2;
			} else if (effectiveWeight <= 100) {
				weightFactor = 1.3;
			} else if (effectiveWeight <= 200) {
				weightFactor = 1.4;
			} else if (effectiveWeight <= 300) {
				weightFactor = 1.5;
			} else if (effectiveWeight <= 400) {
				weightFactor = 1.6;
			}
			else if (effectiveWeight <= 500) {
				weightFactor = 1.7;
			}else if (effectiveWeight <= 600) {
				weightFactor = 1.7;
			}else if (effectiveWeight <= 800) {
				weightFactor = 1.8;
			}else if (effectiveWeight <= 1000) {
				weightFactor = 1.9;
			} else {

			    // After 1000kg:
			    // every extra 200kg adds +0.1

			    double extraWeight = effectiveWeight - 1000.0;

			    int slabs = (int) Math.ceil(extraWeight / 200.0);

			    weightFactor = 2.1 + (slabs * 0.1);

			    // Optional max limit till 3000kg
			    if (effectiveWeight > 3000) {
			        weightFactor = 3.1;
			    }

			    // Round to 1 decimal
			    weightFactor = Math.round(weightFactor * 10.0) / 10.0;
			}
			}
			// ✅ Step 3: Distance pricing
//			double perKmRate = (distanceInKm <= 75) ? 10 : 7;
			double perKmRate;

			if (distanceInKm <= 50) {
			    perKmRate = 10;
			} else if (distanceInKm <= 300) {
			    perKmRate = 4;
			} else if (distanceInKm <= 700) {
			    perKmRate = 2.5;
			} else {
			    perKmRate = 1.5;
			}

			// ✅ Step 4: Final ride cost
			double rideCalculation = distanceInKm * perKmRate * weightFactor * ccFactor * sizeFactor;
			double loadingUnloading =0.0;
			double packaging =0.0;
			
			if(distanceInKm >=100 && effectiveWeight>100) {

			 loadingUnloading = 500.0 * ccFactor;
			 packaging = 500.0 * ccFactor;
			
			}

			double gst = rideCalculation * 0.18;

			double totalCost = rideCalculation + gst + loadingUnloading + packaging;

			rideSummary.setRideCost(rideCalculation);
			rideSummary.setGst(gst);
			rideSummary.setLoadingUnloading(loadingUnloading);
			rideSummary.setPackaging(packaging);
			rideSummary.setTotalCost(totalCost);

//		} catch (Exception e) {
//			e.printStackTrace();
//		}

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


	public Subscription getSubscriptionsByVendor(Long vendorId) {
		Subscription payment=PaymentRepo.findByVendorId(vendorId);
		return payment;
	}


	@Transactional
	public void renewSubscription(Long vendorId) {

		Subscription payment =
	    		PaymentRepo.findByVendorId(vendorId);

	    if (payment == null) {
	        throw new RuntimeException(
	                "Subscription not found");
	    }
	    LocalDate localdate=LocalDate.now();
	    payment.setStartDate(localdate);

	    Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.MONTH, 1);

	    payment.setEndDate(localdate);

	    PaymentRepo.save(payment);
	}
}