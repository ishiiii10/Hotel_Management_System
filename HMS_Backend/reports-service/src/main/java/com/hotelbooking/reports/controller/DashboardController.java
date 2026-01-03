package com.hotelbooking.reports.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.reports.dto.DashboardResponse;
import com.hotelbooking.reports.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Manager Dashboard - shows data for manager's hotel only
     */
    @GetMapping("/manager")
    public ResponseEntity<?> getManagerDashboard(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-Hotel-Id") Long hotelId
    ) {
        if (!"MANAGER".equalsIgnoreCase(role) && !"RECEPTIONIST".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only MANAGER or RECEPTIONIST can access manager dashboard");
        }

        if (hotelId == null) {
            throw new IllegalStateException("Hotel ID is required for manager dashboard");
        }

        DashboardResponse dashboard = dashboardService.getManagerDashboard(hotelId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Manager dashboard retrieved successfully",
                "data", dashboard
        ));
    }

    /**
     * Admin Dashboard - shows data for all hotels or filtered by hotelId
     */
    @GetMapping("/admin")
    public ResponseEntity<?> getAdminDashboard(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) Long hotelId
    ) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can access admin dashboard");
        }

        DashboardResponse dashboard = dashboardService.getAdminDashboard(hotelId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Admin dashboard retrieved successfully",
                "data", dashboard
        ));
    }
}

