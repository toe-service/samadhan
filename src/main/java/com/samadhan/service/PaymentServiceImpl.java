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
import com.samadhan.enums.DimensionUnit;
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

//	public RideCostSummary getrideCostCalculation(String pickuplatitude, String pickuplongitude,
//			String destinationlatitude, String destinationlongitude, ParcelTypeEnum parcelType, CarModelEnum carModel,
////			BikeModelEnum bikeModel, Double parcelWeight, String cc, Double length, Double width, Double heigth) throws JsonMappingException, JsonProcessingException {
//
//		String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" + pickuplatitude + ","
//				+ pickuplongitude + "&destination=" + destinationlatitude + "," + destinationlongitude
//				+ "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";
//
//		RestTemplate restTemplate = new RestTemplate();
//		String response = restTemplate.getForObject(url, String.class);
//
//		RideCostSummary rideSummary = new RideCostSummary();
//
//	//	try {
//			ObjectMapper mapper = new ObjectMapper();
//			JsonNode root = mapper.readTree(response);
//
//			int distanceInMeters = root.path("routes").get(0).path("legs").get(0).path("distance").path("value")
//					.asInt();
//
//			double distanceInKm = distanceInMeters / 1000.0;
//			
////			if (distanceInKm < 50) {
////			    throw new RuntimeException(
////			        "Service is available only for distances greater than or equal to 50 KM"
////			    );
////			}
////			double volumeCubicInches = length * width * heigth;
////			
////			if(volumeCubicInches < 3000.0) {
////				throw new RuntimeException(
////				        "Service is available only for package size greater than or equal to 22 × 18 × 12 Inch"
////				    );
////			}
//			
//			double ccFactor = 1.0;
//
//			if (bikeModel != null && cc != null && !cc.isEmpty()) {
//
//				  int bikeCC = Integer.parseInt(cc);
//
//			    if (bikeCC <= 100) {
//			        ccFactor = 1.0;
//			    }else if (bikeCC <= 199) {
//			        ccFactor = 1.1;
//			    }
//			    else if (bikeCC <= 249) {
//			        ccFactor = 1.2;
//			    }
//			    else if (bikeCC <= 349) {
//			        ccFactor = 1.3;
//			    }
//			    else if (bikeCC <= 449) {
//			        ccFactor = 1.4;
//			    }
//			    else if (bikeCC <= 599) {
//			        ccFactor = 1.5;
//			    }else if (bikeCC <= 799) {
//			        ccFactor = 1.6;
//			    }else if (bikeCC <= 999) {
//			        ccFactor = 1.7;
//			    } else {
//			        ccFactor = 1.8;
//			    }
//			}
//			
//		
//
//			if (carModel != null && cc != null && !cc.isEmpty()) {
//
//			    int carCC = Integer.parseInt(cc);
//
//			    if (carCC <= 799) {
//			        ccFactor = 1.6;
//			    }
//			    else if (carCC <= 999) {
//			        ccFactor = 1.7;
//			    }
//			    else if (carCC <= 1199) {
//			        ccFactor = 1.8;
//			    }
//			    else if (carCC <= 1399) {
//			        ccFactor = 1.9;
//			    }
//			    else if (carCC <= 1599) {
//			        ccFactor = 2.0;
//			    }
//			    else if (carCC <= 1799) {
//			        ccFactor = 2.1;
//			    }
//			    else if (carCC <= 2199) {
//			        ccFactor = 2.2;
//			    }
//			    else if (carCC <= 2999) {
//			        ccFactor = 2.3;
//			    }
//			    else {
//			        ccFactor = 2.4;
//			    }
//			    
//			}
//			
//
//			// ✅ Step 1: Get weight
//			Double effectiveWeight = 0.0;
//
//			if (parcelType != null && parcelType.getType().equalsIgnoreCase("Package")) {
//				effectiveWeight = parcelWeight;
//			} else if (carModel != null) {
//				effectiveWeight = carModel.getAverageWeightKg();
//			} else if (bikeModel != null) {
//				effectiveWeight = bikeModel.getAverageWeightKg();
//			}
//			Double weightFactor = 1.0;
//			double sizeFactor = 1.0;
//			if (parcelType != null && parcelType.getType().equalsIgnoreCase("Package")) {
//				double volumeCubicInches = length * width * heigth;
//				
//				if(volumeCubicInches < 3000.0) {
//					throw new RuntimeException(
//					        "Service is available only for package size greater than or equal to 22 × 18 × 12 Inch"
//					    );
//				}
//				
//
//				if (volumeCubicInches <= 3000) {
//				    sizeFactor = 1.0;
//				} else if (volumeCubicInches <= 6000) {
//				    sizeFactor = 1.2;
//				} else if (volumeCubicInches <= 12000) {
//				    sizeFactor = 1.40;
//				} else if (volumeCubicInches <= 20000) {
//				    sizeFactor = 1.50;
//				} else {
//				    sizeFactor = 1.75;
//				}
//				
//				
//			// ✅ Step 2: Weight factor
//			
//			if (effectiveWeight <= 5) {
//				weightFactor = 1.0;
//			} else if (effectiveWeight <= 20) {
//				weightFactor = 1.1;
//			} else if (effectiveWeight <= 50) {
//				weightFactor = 1.2;
//			} else if (effectiveWeight <= 100) {
//				weightFactor = 1.3;
//			} else if (effectiveWeight <= 200) {
//				weightFactor = 1.4;
//			} else if (effectiveWeight <= 300) {
//				weightFactor = 1.5;
//			} else if (effectiveWeight <= 400) {
//				weightFactor = 1.6;
//			}
//			else if (effectiveWeight <= 500) {
//				weightFactor = 1.7;
//			}else if (effectiveWeight <= 600) {
//				weightFactor = 1.7;
//			}else if (effectiveWeight <= 800) {
//				weightFactor = 1.8;
//			}else if (effectiveWeight <= 1000) {
//				weightFactor = 1.9;
//			} else {
//
//			    // After 1000kg:
//			    // every extra 200kg adds +0.1
//
//			    double extraWeight = effectiveWeight - 1000.0;
//
//			    int slabs = (int) Math.ceil(extraWeight / 200.0);
//
//			    weightFactor = 2.1 + (slabs * 0.1);
//
//			    // Optional max limit till 3000kg
//			    if (effectiveWeight > 3000) {
//			        weightFactor = 3.1;
//			    }
//
//			    // Round to 1 decimal
//			    weightFactor = Math.round(weightFactor * 10.0) / 10.0;
//			}
//			}
//			// ✅ Step 3: Distance pricing
////			double perKmRate = (distanceInKm <= 75) ? 10 : 7;
//			double perKmRate;
//
//			if (distanceInKm <= 50) {
//			    perKmRate = 10;
//			}if (distanceInKm <= 120) {
//			    perKmRate = 7;
//			}if (distanceInKm <= 200) {
//			    perKmRate = 6;
//			}
//			else if (distanceInKm <= 300) {
//			    perKmRate = 5;
//			} else if (distanceInKm <= 700) {
//			    perKmRate = 4;
//			} else if (distanceInKm <= 1000) {
//			    perKmRate = 3;
//			} else {
//			    perKmRate = 2.5;
//			}
//
//			// ✅ Step 4: Final ride cost
//			double rideCalculation = distanceInKm * perKmRate * weightFactor * ccFactor * sizeFactor;
//			double loadingUnloading =0.0;
//			double packaging =0.0;
//			
//			if(distanceInKm >=100 && effectiveWeight>100) {
//
//			 loadingUnloading = 500.0 * ccFactor;
//			 packaging = 500.0 * ccFactor;
//			
//			}
//
//			double gst = rideCalculation * 0.18;
//
//			double totalCost = rideCalculation + gst + loadingUnloading + packaging;
//
//			rideSummary.setRideCost(rideCalculation);
//			rideSummary.setGst(gst);
//			rideSummary.setLoadingUnloading(loadingUnloading);
//			rideSummary.setPackaging(packaging);
//			rideSummary.setTotalCost(totalCost);
//
////		} catch (Exception e) {
////			e.printStackTrace();
////		}
//
//		return rideSummary;
//	}
    
    
    
	public RideCostSummary getrideCostCalculation(String pickuplatitude, String pickuplongitude,
			String destinationlatitude, String destinationlongitude, ParcelTypeEnum parcelType, CarModelEnum carModel,
			BikeModelEnum bikeModel, Double parcelWeight, String cc, Double length, Double width, Double heigth, DimensionUnit dimensionUnit) 
					throws JsonMappingException, JsonProcessingException {

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
			
			//---------------------------------------------------------
		    // PER KM RATE
		    //---------------------------------------------------------

		    double perKmRate = 0;
		    
		    if (parcelType == ParcelTypeEnum.Bike || parcelType == ParcelTypeEnum.Car) {
		    	
		    	
		   

		    if (distanceInKm <= 50) {

		        perKmRate = 10;

		    } else if (distanceInKm <= 200) {

		        perKmRate = 9;

		    } else if (distanceInKm <= 350) {

		        perKmRate = 8;

		    }
		    else if (distanceInKm <= 600) {

		        perKmRate = 7;

		    } else if (distanceInKm <= 1000) {

		        perKmRate = 6;

		    } else if (distanceInKm <= 5000) {

		        perKmRate = 5;

		    } 
		    else {

		        perKmRate = 4.5;

		    }
		    
		    }

		    //---------------------------------------------------------
		    // COMMON VARIABLES
		    //---------------------------------------------------------

		    double distanceCharge = distanceInKm * perKmRate;

		    double fixedCharge = 0;

		    double packaging = 0;

		    double loadingUnloading = 0;
		    
		    double weightCharge=0;

		    //---------------------------------------------------------
		    // PACKAGE PRICING
		    //---------------------------------------------------------
		    
		    
		  //---------------------------------------------------------
		 // PACKAGE PRICING
		 //---------------------------------------------------------

		 if (parcelType == ParcelTypeEnum.Package) {

//		     double volume = length * width * heigth;

		     //-------------------------------------------------
		     // Distance Rate (Bulk Transportation)
		     //-------------------------------------------------

		     if (distanceInKm <= 50) {
		         perKmRate = 2.8;
		     } else if (distanceInKm <= 200) {
		         perKmRate = 1.6;
		     } else if (distanceInKm <= 500) {
		         perKmRate = 1.1;
		     } else if (distanceInKm <= 1000) {
		         perKmRate = 0.75;
		     } else {
		         perKmRate = 0.55;
		     }

		     distanceCharge = distanceInKm * perKmRate;

		     //-------------------------------------------------
		     // Volumetric Weight
		     //-------------------------------------------------
		     
		     double lengthInch = length;
		     double widthInch = width;
		     double heightInch = heigth;

		     if (dimensionUnit == DimensionUnit.CM) {
		         lengthInch /= 2.54;
		         widthInch /= 2.54;
		         heightInch /= 2.54;
		     } else if (dimensionUnit == DimensionUnit.FT) {
		         lengthInch *= 12;
		         widthInch *= 12;
		         heightInch *= 12;
		     }

		     double volume = lengthInch * widthInch * heightInch;

		     

		     double volumetricWeight = volume / 250.0;

		     double chargeableWeight =
		             Math.max(parcelWeight, volumetricWeight);

		     //-------------------------------------------------
		     // Base Handling Charge
		     //-------------------------------------------------

		     fixedCharge = 150;

		     //-------------------------------------------------
		     // Weight Charge
		     //-------------------------------------------------

		      weightCharge = chargeableWeight * 15;

		     //-------------------------------------------------
		     // Packaging Charge
		     //-------------------------------------------------

		     packaging = 0;

		     if (!(parcelWeight <= 5 && volume <= 1500)) {

		         if (volume <= 4000) {
		             packaging = 60;
		         }
		         else if (volume <= 8000) {
		             packaging = 120;
		         }
		         else if (volume <= 15000) {
		             packaging = 220;
		         }
		         else if (volume <= 25000) {
		             packaging = 350;
		         }
		         else {
		             packaging = 500;
		         }
		     }

		     //-------------------------------------------------
		     // Loading / Unloading
		     //-------------------------------------------------

		     loadingUnloading = 0;

		     if (chargeableWeight > 20) {

		         loadingUnloading = chargeableWeight * 8;

		         if (loadingUnloading > 500) {
		             loadingUnloading = 500;
		         }
		     }

		     //-------------------------------------------------
		     // Final Ride Charge
		     //-------------------------------------------------

//		     rideCalculation =
//		             fixedCharge
//		             + distanceCharge
//		             + weightCharge;
		 }
		    
		    

//		    if (parcelType == ParcelTypeEnum.Package) {
//
//		        double volume = length * width * heigth;
//
////		        if (volume < 3000) {
////
////		            throw new RuntimeException(
////		                    "Minimum package size should be 22 × 18 × 12 Inch");
////
////		        }
//
//		        //-------------------------------------------------
//		        // Small Package
//		        //-------------------------------------------------
//
//		        if (parcelWeight <= 5 && volume <= 1000) {
//
//		            fixedCharge = 80;
//
//		            packaging = 0;
//
//		            loadingUnloading = 0;
//
//		        }
//		        
//		        else if (parcelWeight <= 5 && volume <= 5000) {
//		        	 fixedCharge = 100;
//		            packaging = 50;
//		        }
//
//		        //-------------------------------------------------
//		        // Medium Package
//		        //-------------------------------------------------
//
//		        else if (parcelWeight <= 20 && volume <= 8000) {
//
//		            fixedCharge = 150;
//
//		            packaging = 100;
//
//		            loadingUnloading = 50;
//
//		        }
//		        
//		        else if (parcelWeight <= 20 && volume <= 12000) {
//		        	  fixedCharge = 200;
//
//			          packaging = 120;
//
//			          loadingUnloading = 50;
//		        }
//
//		        //-------------------------------------------------
//		        // Large Package
//		        //-------------------------------------------------
//
//		        else if (parcelWeight <= 50 && volume <= 15000) {
//
//		            fixedCharge = 250;
//
//		            packaging = 150;
//
//		            loadingUnloading = 100;
//
//		        }
//
//		        //-------------------------------------------------
//		        // Heavy Package
//		        //-------------------------------------------------
//
//		        else if (parcelWeight <= 100) {
//
//		            fixedCharge = 500;
//
//		            packaging = 250;
//
//		            loadingUnloading = 150;
//
//		        }
//
//		        //-------------------------------------------------
//		        // Commercial Package
//		        //-------------------------------------------------
//
//		        else if (parcelWeight <= 250) {
//
//		            fixedCharge = 900;
//
//		            packaging = 400;
//
//		            loadingUnloading = 250;
//
//		        }
//
//		        //-------------------------------------------------
//		        // Industrial Package
//		        //-------------------------------------------------
//
//		        else if (parcelWeight <= 500) {
//
//		            fixedCharge = 1500;
//
//		            packaging = 700;
//
//		            loadingUnloading = 400;
//
//		        }
//
//		        //-------------------------------------------------
//		        // Above 500 KG
//		        //-------------------------------------------------
//
//		        else {
//
//		            fixedCharge = 2000;
//
//		            packaging = 900;
//
//		            loadingUnloading = 500;
//
//		            double extraWeight = parcelWeight - 500;
//
//		            int slabs = (int) Math.ceil(extraWeight / 100);
//
//		            fixedCharge += slabs * 200;
//
//		        }
//		    }
			
			
		    //---------------------------------------------------------
		    // BIKE PRICING
		    //---------------------------------------------------------

		    else if (parcelType == ParcelTypeEnum.Bike) {

		    	if (cc != null && !cc.isEmpty()) {
		    	
		        int bikeCC = Integer.parseInt(cc);

		        // Fixed transportation charge based on CC

		        if (bikeCC <= 100) {

		            fixedCharge = 500;
		            packaging = 400;
		            loadingUnloading = 300;

		        }
		        else if (bikeCC <= 125) {

		            fixedCharge = 600;
		            packaging = 450;
		            loadingUnloading = 300;

		        }
		        else if (bikeCC <= 150) {

		            fixedCharge = 800;
		            packaging = 500;
		            loadingUnloading = 300;

		        }
		        else if (bikeCC <= 200) {

		            fixedCharge = 1200;
		            packaging = 600;
		            loadingUnloading = 400;

		        }
		        else if (bikeCC <= 350) {

		            // Royal Enfield / KTM

		            fixedCharge = 1500;
		            packaging = 750;
		            loadingUnloading = 500;

		        }
		        else if (bikeCC <= 500) {

		            fixedCharge = 2000;
		            packaging = 1000;
		            loadingUnloading = 600;

		        }
		        else if (bikeCC <= 800) {

		            fixedCharge = 2200;
		            packaging = 1200;
		            loadingUnloading = 600;

		        }
		        else if (bikeCC <= 1200) {

		            fixedCharge = 2500;
		            packaging = 1200;
		            loadingUnloading = 700;

		        }  else if (bikeCC <= 2000) {

		            fixedCharge = 3000;
		            packaging = 1200;
		            loadingUnloading = 900;

		        }
		        else {

		            // Hayabusa / BMW / ZX10R etc.

		            fixedCharge = 3200;
		            packaging = 1200;
		            loadingUnloading = 1000;

		        }

		        // Extra long-distance handling

//		        if (distanceInKm > 700) {
//
//		            fixedCharge += 00;
//
//		        }

//		        if (distanceInKm > 1200) {
//
//		            fixedCharge += 1000;
//
//		        }
		        
		      }else {
		    	  fixedCharge = 600;
		    	  packaging = 450;
		    	  loadingUnloading = 300;
		      }

		    }

		    //---------------------------------------------------------
		    // CAR PRICING
		    //---------------------------------------------------------

		    else if (parcelType == ParcelTypeEnum.Car) {
		    	
		    	if (cc != null && !cc.isEmpty()) {
		    	

		        int carCC = Integer.parseInt(cc);

		        if (carCC <= 1000) {

		            // Alto / Kwid

		            fixedCharge = 2500;
		            packaging = 0;
		            loadingUnloading = 900;

		        }
		        else if (carCC <= 1500) {

		            // Punch / Tiago / Baleno

		            fixedCharge = 3500;
		            packaging = 0;
		            loadingUnloading = 1000;

		        }
		        else if (carCC <= 2000) {

		            // Nexon / Brezza / Creta / City

		            fixedCharge = 5000;
		            packaging = 0;
		            loadingUnloading = 1200;

		        }
		        else if (carCC <= 3000) {

		            fixedCharge = 8000;
		            packaging = 0;
		            loadingUnloading = 1400;

		        }
		        else if (carCC <= 4000) {

		            // Harrier / XUV700

		            fixedCharge = 12000;
		            packaging = 0;
		            loadingUnloading = 2000;

		        }
		        else if (carCC <= 5000) {

		            // Fortuner

		            fixedCharge = 15000;
		            packaging = 0;
		            loadingUnloading = 2000;

		        }
		        else {

		            // BMW / Mercedes / Audi / Land Rover

		            fixedCharge = 18000;
		            packaging = 0;
		            loadingUnloading = 2500;

		        }

		        // Long-distance handling

		        if (distanceInKm > 700) {

		            fixedCharge += 1500;

		        }

		        if (distanceInKm > 1200) {

		            fixedCharge += 2500;

		        }
		        
		      }else {
		    	  
		    	   fixedCharge = 6000;
		    	   packaging = 0;
		    	   loadingUnloading = 900;
		    	  
		      }

		    }
		 
		  double rideCalculation=0.0;
		 if(parcelType == ParcelTypeEnum.Car || parcelType == ParcelTypeEnum.Bike) {
		    //---------------------------------------------------------
		    // FINAL PRICE CALCULATION
		    //---------------------------------------------------------

		     rideCalculation = fixedCharge + distanceCharge;

		    //---------------------------------------------------------
		    // LONG DISTANCE SURCHARGE
		    //---------------------------------------------------------

		    if (distanceInKm > 500 && distanceInKm <= 800) {

		        rideCalculation += 500;

		    } else if (distanceInKm > 800 && distanceInKm <= 1200) {

		        rideCalculation += 1000;

		    } else if (distanceInKm > 1200) {

		        rideCalculation += 1500;

		    }
		    
		 }else if(parcelType == ParcelTypeEnum.Package) {
			 
			  rideCalculation =
			             fixedCharge
			             + distanceCharge
			             + weightCharge;
		 }

		    //---------------------------------------------------------
		    // GST
		    //---------------------------------------------------------

		    double gst = rideCalculation * 0.18;

		    //---------------------------------------------------------
		    // TOTAL
		    //---------------------------------------------------------

		    double totalCost =
		            rideCalculation
		            + gst
		            + packaging
		            + loadingUnloading;

		    //---------------------------------------------------------
		    // ROUND VALUES
		    //---------------------------------------------------------

		    rideCalculation = Math.round(rideCalculation);
		    gst = Math.round(gst);
		    packaging = Math.round(packaging);
		    loadingUnloading = Math.round(loadingUnloading);
		    totalCost = Math.round(totalCost);

		    //---------------------------------------------------------
		    // RESPONSE
		    //---------------------------------------------------------

		  
		    rideSummary.setRideCost(rideCalculation);

		 
		    rideSummary.setPackaging(packaging);

		    rideSummary.setLoadingUnloading(loadingUnloading);

		    rideSummary.setGst(gst);

		    rideSummary.setTotalCost(totalCost);

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