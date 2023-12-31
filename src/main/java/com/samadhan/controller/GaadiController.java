package com.samadhan.controller;


import com.samadhan.request.LoginRequest;
import com.samadhan.response.GeneralResponse;
import com.samadhan.service.GaadiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/gaadi-dikhao")
public class GaadiController {

    @Autowired
    private GaadiService gaadiService;

    @GetMapping(value = "/status")
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("service is up");
    }

    @PostMapping(value =  "/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) throws Exception {
       String resp = gaadiService.login(loginRequest);
       return ResponseEntity.ok(resp);
    }

    @GetMapping(value = "/verify-token")
    public ResponseEntity<GeneralResponse> verifyToken(@RequestParam String token) {
        GeneralResponse generalResponse = gaadiService.verifyToken(token);
        return ResponseEntity.status(generalResponse.statusCode).body(generalResponse);
    }
}
