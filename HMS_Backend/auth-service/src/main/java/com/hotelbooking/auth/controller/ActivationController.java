package com.hotelbooking.auth.controller;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.auth.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ActivationController {

    private final UserService userService;

    @PostMapping("/activate")
    public ResponseEntity<String> activate(@RequestParam String token) {
        userService.activateUser(token);
        return ResponseEntity.ok("Account activated successfully");
    }
}