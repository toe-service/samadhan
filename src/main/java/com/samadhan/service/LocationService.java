package com.samadhan.service;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

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
}
