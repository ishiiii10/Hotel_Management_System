package com.hotelbooking.notification.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.hotelbooking.notification.dto.UserInfoResponse;

@FeignClient(name = "AUTH-SERVICE", path = "/internal/auth")
public interface AuthServiceClient {

    @GetMapping("/users/{userId}")
    UserInfoResponse getUserById(@PathVariable Long userId);
}

