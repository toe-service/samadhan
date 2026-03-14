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

import org.springframework.stereotype.Service;

@Service
public class LocationService {
	
	private String apiKey="AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";

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

            String address = node
                    .get("results")
                    .get(0)
                    .get("formatted_address")
                    .asText();

            result.put("latitude", lat);
            result.put("longitude", lng);
            result.put("address", address);

        } catch (Exception e) {
            result.put("error", "Address not found");
        }

        return result;
    }

	public List<String> searchLocation(String input) throws JsonMappingException, JsonProcessingException, RestClientException {
		 String url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input="
	                + input + "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";

	        RestTemplate restTemplate = new RestTemplate();

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(restTemplate.getForObject(url, String.class));

	        JsonNode predictions = root.path("predictions");

	        List<String> result = new ArrayList<>();

	        for (int i = 0; i < Math.min(10, predictions.size()); i++) {
	            result.add(predictions.get(i).get("description").asText());
	        }

	        return result;
	}

	public Map<String, Double> getLatLong(String address) throws UnsupportedEncodingException, JsonMappingException, JsonProcessingException {
		 String url = "https://maps.googleapis.com/maps/api/geocode/json?address="
	                + URLEncoder.encode(address, "UTF-8")
	                + "&key=AIzaSyBEPIJBBKO6Xg8sqvAByFrWcShWVNSdVyM";

	        RestTemplate restTemplate = new RestTemplate();
	        String response = restTemplate.getForObject(url, String.class);

	        ObjectMapper mapper = new ObjectMapper();
	        JsonNode root = mapper.readTree(response);

	        JsonNode location = root.path("results")
	                                .get(0)
	                                .path("geometry")
	                                .path("location");

	        double lat = location.get("lat").asDouble();
	        double lng = location.get("lng").asDouble();

	        Map<String, Double> result = new HashMap<>();
	        result.put("latitude", lat);
	        result.put("longitude", lng);

	        return result;
	}
}
