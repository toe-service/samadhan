package com.samadhan.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.samadhan.service.LocationService;

@RestController
@RequestMapping("/location")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service){
        this.service = service;
    }

    @GetMapping
    public Map<String, Object> getLocation(@RequestParam double lat,
                              @RequestParam double lng) {

        return service.getLocation(lat, lng);
    }
}
