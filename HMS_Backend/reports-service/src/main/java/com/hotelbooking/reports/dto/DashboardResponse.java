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
public class DashboardResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private Long totalBookings;
    private Long totalCheckIns;
    private Long totalCheckOuts;
    private Double averageRating;
    private List<RevenueByHotel> revenueByHotel;
    private List<RevenueTrend> revenueTrend;
    private List<BookingTrend> bookingTrend;
    private List<BookingStatusDistribution> bookingStatusDistribution;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueByHotel implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long hotelId;
        private String hotelName;
        private BigDecimal revenue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RevenueTrend implements Serializable {
        private static final long serialVersionUID = 1L;

        private String date;
        private BigDecimal amount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingTrend implements Serializable {
        private static final long serialVersionUID = 1L;

        private String date;
        private Long count;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingStatusDistribution implements Serializable {
        private static final long serialVersionUID = 1L;

        private String status;
        private Long count;
    }
}
