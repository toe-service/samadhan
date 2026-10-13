package com.samadhan.service;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

	private static final Logger logger = LoggerFactory.getLogger(LocationService.class);

	private String apiKey="AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";

	@Cacheable(value = "LatLongCache", key = "#lat + '-' + #lng")
    public Map<String, Object> getLocation(double lat, double lng) {

        String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                + lat + "," + lng + "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";
        System.out.println(url);

        RestTemplate restTemplate = new RestTemplate();

        String response = restTemplate.getForObject(url, String.class);

        Map<String, Object> result = new HashMap<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(response);
            if (node.size() > 0) {
	            String address = node
	                    .get("results")
	                    .get(0)
	                    .get("formatted_address")
	                    .asText();
	
	            result.put("latitude", lat);
	            result.put("longitude", lng);
	            result.put("address", address);
            } else {
                result.put("error", "Address not found");
            }

        } catch (Exception e) {
            logger.warn("Failed to resolve address for lat={}, lng={}: {}", lat, lng, e.getMessage());
            result.put("error", "Address not found");
        }

        return result;
    }

	// Used by PaymentController#generateInvoice to decide CGST+SGST vs IGST — needs the actual
	// state, not a guess parsed out of a free-text address string. CityUtils.extractCity's own
	// comment already warns that TransferRequestDetails.source/destination don't reliably follow
	// a parseable "...,City,State,Country" shape, so this reuses the same structured Google
	// geocode lookup (and cache) as getLocation above instead, reading administrative_area_level_1
	// from address_components rather than string-splitting the formatted address.
	@Cacheable(value = "StateCache", key = "#lat + '-' + #lng")
	public String getState(double lat, double lng) {

		String url = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
				+ lat + "," + lng + "&key=" + apiKey;

		RestTemplate restTemplate = new RestTemplate();

		try {
			String response = restTemplate.getForObject(url, String.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response);
			JsonNode results = root.path("results");
			if (results.size() == 0) {
				return null;
			}

			for (JsonNode component : results.get(0).path("address_components")) {
				for (JsonNode type : component.path("types")) {
					if ("administrative_area_level_1".equals(type.asText())) {
						return component.get("long_name").asText();
					}
				}
			}
			return null;
		} catch (Exception e) {
			logger.warn("Failed to resolve state for lat={}, lng={}: {}", lat, lng, e.getMessage());
			return null;
		}
	}

	@Cacheable(value = "SearchLocationCache", key = "#input")
	public List<String> searchLocation(String input) throws JsonMappingException, JsonProcessingException, RestClientException {
		 
		List<String> result = new ArrayList<>();
		 if (input == null || input.trim().length() < 4) {
		        return result;
		    }
		
		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
	                + input + "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM"
	                 + "&components=country:in";   // India only

	        RestTemplate restTemplate = new RestTemplate();

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(restTemplate.getForObject(url, String.class));

	        JsonNode predictions = root.path("predictions");

	      //  List<String> result = new ArrayList<>();

	        for (int i = 0; i < Math.min(10, predictions.size()); i++) {
	            result.add(predictions.get(i).get("description").asText());
	        }

	        return result;
	}

	@Cacheable(value = "GetAddressCache", key = "#address")
	public Map<String, Object> getLatLong(String address)
	        throws UnsupportedEncodingException,
	        JsonProcessingException {

	    RestTemplate restTemplate = new RestTemplate();
	    ObjectMapper mapper = new ObjectMapper();

	    // Plain Geocoding API matches struggle with society/plot-number addresses (e.g.
	    // "A-308, Plot No. GH-02A, Sector 76, Noida") and silently fall back to an
	    // approximate nearby point. Autocomplete -> Place Details resolves to the exact
	    // place a user would get by picking the address from a suggestions dropdown, so
	    // try that first and only fall back to the original plain-geocode behavior below
	    // if no prediction is found, keeping this a pure accuracy improvement.
	    JsonNode placeDetailsResult = resolveViaPlaceDetails(address, restTemplate, mapper);

	    JsonNode resultNode;
	    if (placeDetailsResult != null) {
	        resultNode = placeDetailsResult;
	    } else {
	        String url =
	                "https://maps.googleapis.com/maps/api/geocode/json?address="
	                        + URLEncoder.encode(address, "UTF-8")
	                        + "&key=" + apiKey;

	        String response =
	                restTemplate.getForObject(url, String.class);

	        JsonNode root = mapper.readTree(response);

	        resultNode =
	                root.path("results").get(0);
	    }

	    JsonNode location =
	            resultNode.path("geometry")
	                    .path("location");

	    double lat = location.get("lat").asDouble();

	    double lng = location.get("lng").asDouble();

	    // Extract pincode
	    String pincode = "";

	    JsonNode addressComponents =
	            resultNode.path("address_components");

	    for (JsonNode component : addressComponents) {

	        JsonNode types = component.get("types");

	        for (JsonNode type : types) {

	            if (type.asText().equals("postal_code")) {

	                pincode =
	                        component.get("long_name").asText();

	                break;
	            }
	        }

	        if (!pincode.isEmpty()) {
	            break;
	        }
	    }

	    Map<String, Object> result = new HashMap<>();

	    result.put("latitude", lat);
	    result.put("longitude", lng);
	    result.put("pincode", pincode);

	    return result;
	}

	// Best-effort: returns null on any failure (no predictions, API error, malformed
	// response) so getLatLong always has its original plain-geocode behavior to fall back on.
	private JsonNode resolveViaPlaceDetails(String address, RestTemplate restTemplate, ObjectMapper mapper) {
		try {
			String autocompleteUrl =
					"https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
							+ URLEncoder.encode(address, "UTF-8")
							+ "&key=" + apiKey
							+ "&components=country:in";

			JsonNode autocompleteRoot = mapper.readTree(restTemplate.getForObject(autocompleteUrl, String.class));
			JsonNode predictions = autocompleteRoot.path("predictions");
			if (predictions.size() == 0) {
				return null;
			}

			String placeId = predictions.get(0).path("place_id").asText(null);
			if (placeId == null) {
				return null;
			}

			String detailsUrl =
					"https://maps.googleapis.com/maps/api/place/details/json?place_id="
							+ placeId
							+ "&fields=geometry,address_component"
							+ "&key=" + apiKey;

			JsonNode detailsRoot = mapper.readTree(restTemplate.getForObject(detailsUrl, String.class));
			if (!"OK".equals(detailsRoot.path("status").asText())) {
				return null;
			}

			JsonNode result = detailsRoot.path("result");
			return result.path("geometry").path("location").has("lat") ? result : null;
		} catch (Exception e) {
			logger.warn("Autocomplete->Place Details lookup failed for address '{}': {}", address, e.getMessage());
			return null;
		}
	}

	public List<String> sourceSearchLocation(String input) throws JsonMappingException, JsonProcessingException, RestClientException {
		
		List<String> result = new ArrayList<>();
		 if (input == null || input.trim().length() < 4) {
		        return result;
		    }
		
		String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
		        + input
		        + "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM"
		        + "&components=country:in";   // India only
//		        + "&location=28.6139,77.2090" // Delhi center
//		        + "&radius=50000";           // 50 km (NCR range)
		
		
		  RestTemplate restTemplate = new RestTemplate();
		
		 ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(restTemplate.getForObject(url, String.class));

	        JsonNode predictions = root.path("predictions");

		List<String> allowedCities = Arrays.asList(
		        "Delhi", "Noida", "Greater Noida", "Ghaziabad", "Gurgaon", "Gurugram", "Faridabad", "Amethi", "Lucknow",
		        "Kanpur", "Agra", "Meerut", "Hapur", "Kannauj", "Varanasi", "Banaras", "Etawah", "Barabanki",
		        "Sultanpur", "Mathura", "Rampur"
		);

		for (int i = 0; i < Math.min(10, predictions.size()); i++) {

		    String location = predictions.get(i).get("description").asText();
		    
		    System.out.println(location);

		    boolean isAllowed = allowedCities.stream()
		            .anyMatch(city -> location.toLowerCase().contains(city.toLowerCase()));

		    if (isAllowed) {
		        result.add(location);
		    }
		}
		
		
	    if (result.isEmpty()) {

	     
	        result.add("We are not serviceable here");
	    }
		
		return result;
	}
}
