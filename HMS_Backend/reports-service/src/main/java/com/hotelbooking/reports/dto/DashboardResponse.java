package com.hotelbooking.reports.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private Long totalBookings;
    private Long todayBookings;
    private Long checkInsToday;
    private Long checkOutsToday;
    private Long availableRoomsToday;
    private Double averageRating;
    private List<RevenueByHour> todayRevenueByHour;
    private List<BookingsBySource> todayBookingsBySource;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueByHour {
        private Integer hour;
        private BigDecimal amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingsBySource {
        private Integer hour;
        private Long publicBookings;
        private Long walkInBookings;
    }
}

