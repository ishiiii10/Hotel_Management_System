package com.hotelbooking.reports.dto;

import java.io.Serializable;
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
public class HotelReportResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long hotelId;
    private String hotelName;
    private String category;
    private String city;
    private String address;
    private String status;
    private Integer starRating;
    private Long totalBookings;
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Double averageRating;
    private List<RecentBooking> recentBookings;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentBooking implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long bookingId;
        private String guestName;
        private String guestEmail;
        private String checkInDate;
        private String checkOutDate;
        private BigDecimal amount;
        private String status;
    }
}

