package com.hotelbooking.auth.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class HealthController {

    @GetMapping("/ping")
    public String ping() {
        return "Auth service is reachable";
    }
}