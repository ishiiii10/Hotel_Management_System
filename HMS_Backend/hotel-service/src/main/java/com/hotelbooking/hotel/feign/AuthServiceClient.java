package com.hotelbooking.hotel.feign;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.hotelbooking.hotel.dto.request.CreateStaffRequest;

@FeignClient(name = "AUTH-SERVICE", path = "/auth/admin")
public interface AuthServiceClient {

    @PostMapping("/staff")
    Map<String, String> createStaff(@RequestBody CreateStaffRequest request);
}

