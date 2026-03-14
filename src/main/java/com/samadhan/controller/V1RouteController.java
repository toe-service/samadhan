package com.samadhan.controller;

import com.samadhan.dto.RouteRequest;
import com.samadhan.dto.RouteResponse;
import com.samadhan.service.RouteService;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class V1RouteController {
    
	@Autowired
	RouteService routeService;

    @PostMapping("/route")
    public RouteResponse getRoute(@RequestBody RouteRequest request) {
        return routeService.getRoute(request);
    }
}
