package com.hotelbooking.reports.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.reports.dto.HotelReportResponse;
import com.hotelbooking.reports.service.ReportsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportsController {

    private final ReportsService reportsService;

    @GetMapping("/hotels")
    public ResponseEntity<?> getHotelReports(
            @RequestHeader("X-User-Role") String role
    ) {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Only ADMIN can access hotel reports");
        }

        List<HotelReportResponse> reports = reportsService.getHotelReports();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Hotel reports retrieved successfully",
                "data", reports
        ));
    }
}

