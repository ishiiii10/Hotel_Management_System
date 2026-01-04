package com.hotelbooking.reports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.reports.domain.hotel.Hotel;
import com.hotelbooking.reports.dto.HotelReportResponse;
import com.hotelbooking.reports.repository.billing.BillRepository;
import com.hotelbooking.reports.repository.booking.BookingRepository;
import com.hotelbooking.reports.repository.hotel.HotelRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportsService {

    private final HotelRepository hotelRepository;
    private final BillRepository billRepository;
    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Cacheable(value = "reports", key = "'hotel-reports'")
    public List<HotelReportResponse> getHotelReports() {
        List<Hotel> hotels = hotelRepository.findAll();

        return hotels.stream().map(hotel -> {
            Long hotelId = hotel.getId();

            Long totalBookings = bookingRepository.countAllBookings(hotelId);
            if (totalBookings == null) totalBookings = 0L;

            BigDecimal totalRevenue = billRepository.getTotalRevenue(hotelId);
            if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

            LocalDate now = LocalDate.now();
            BigDecimal monthlyRevenue = billRepository.getMonthlyRevenue(
                now.getYear(), now.getMonthValue(), hotelId);
            if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;

            Double averageRating = hotel.getStarRating() != null 
                ? hotel.getStarRating().doubleValue() 
                : 0.0;

            List<HotelReportResponse.RecentBooking> recentBookings = getRecentBookings(hotelId);

            return HotelReportResponse.builder()
                    .hotelId(hotelId)
                    .hotelName(hotel.getName())
                    .category(hotel.getCategory())
                    .city(hotel.getCity())
                    .address(hotel.getAddress())
                    .status(hotel.getStatus())
                    .starRating(hotel.getStarRating())
                    .totalBookings(totalBookings)
                    .totalRevenue(totalRevenue)
                    .monthlyRevenue(monthlyRevenue)
                    .averageRating(averageRating)
                    .recentBookings(recentBookings)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<HotelReportResponse.RecentBooking> getRecentBookings(Long hotelId) {
        List<Object[]> results = bookingRepository.findRecentBookingsByHotel(hotelId);
        return results.stream().map(row -> {
            java.time.LocalDate checkInDate = null;
            java.time.LocalDate checkOutDate = null;
            
            if (row[3] != null) {
                if (row[3] instanceof java.sql.Date) {
                    checkInDate = ((java.sql.Date) row[3]).toLocalDate();
                } else if (row[3] instanceof java.time.LocalDate) {
                    checkInDate = (java.time.LocalDate) row[3];
                } else if (row[3] instanceof java.sql.Timestamp) {
                    checkInDate = ((java.sql.Timestamp) row[3]).toLocalDateTime().toLocalDate();
                }
            }
            
            if (row[4] != null) {
                if (row[4] instanceof java.sql.Date) {
                    checkOutDate = ((java.sql.Date) row[4]).toLocalDate();
                } else if (row[4] instanceof java.time.LocalDate) {
                    checkOutDate = (java.time.LocalDate) row[4];
                } else if (row[4] instanceof java.sql.Timestamp) {
                    checkOutDate = ((java.sql.Timestamp) row[4]).toLocalDateTime().toLocalDate();
                }
            }
            
            return HotelReportResponse.RecentBooking.builder()
                    .bookingId(((Number) row[0]).longValue())
                    .guestName((String) row[1])
                    .guestEmail((String) row[2])
                    .checkInDate(checkInDate != null ? checkInDate.format(DATE_FORMATTER) : null)
                    .checkOutDate(checkOutDate != null ? checkOutDate.format(DATE_FORMATTER) : null)
                    .amount((BigDecimal) row[5])
                    .status((String) row[6])
                    .build();
        }).collect(Collectors.toList());
    }
}

