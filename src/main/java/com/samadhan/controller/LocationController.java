package com.samadhan.controller;

import java.util.*;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.samadhan.service.LocationService;



@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService service){
        this.locationService = service;
    }

    @GetMapping
    public Map<String, Object> getLocation(@RequestParam double lat,
                              @RequestParam double lng) {

        return locationService.getLocation(lat, lng);
    }
    
    @GetMapping("/search")
    public List<String> search(@RequestParam String query) throws JsonMappingException, JsonProcessingException, RestClientException {
        return locationService.searchLocation(query);
    }
    
    @GetMapping("/source/search")
    public List<String> sourceSearch(@RequestParam String query) throws JsonMappingException, JsonProcessingException, RestClientException {
        return locationService.sourceSearchLocation(query);
    }
    
    @GetMapping("/latlong")
    public Map<String, Double> getLatLong(@RequestParam String address) throws Exception {

        return locationService.getLatLong(address);
    }
    
}
