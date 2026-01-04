package com.hotelbooking.reports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.reports.domain.hotel.Hotel;
import com.hotelbooking.reports.dto.DashboardResponse;
import com.hotelbooking.reports.repository.billing.BillRepository;
import com.hotelbooking.reports.repository.booking.BookingRepository;
import com.hotelbooking.reports.repository.hotel.HotelRepository;
import com.hotelbooking.reports.repository.hotel.RoomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardService {

    private final BillRepository billRepository;
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Cacheable(value = "dashboard", key = "'manager:' + #hotelId")
    public DashboardResponse getManagerDashboard(Long hotelId) {
        return buildDashboard(hotelId);
    }

    @Cacheable(value = "dashboard", key = "'admin:' + (#hotelId != null ? #hotelId : 'all')")
    public DashboardResponse getAdminDashboard(Long hotelId) {
        return buildDashboard(hotelId);
    }

    private DashboardResponse buildDashboard(Long hotelId) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        BigDecimal totalRevenue = billRepository.getTotalRevenue(hotelId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal monthlyRevenue = billRepository.getMonthlyRevenue(currentYear, currentMonth, hotelId);
        if (monthlyRevenue == null) monthlyRevenue = BigDecimal.ZERO;

        Long totalBookings = bookingRepository.countAllBookings(hotelId);
        if (totalBookings == null) totalBookings = 0L;

        Long totalCheckIns = bookingRepository.countTotalCheckIns(hotelId);
        if (totalCheckIns == null) totalCheckIns = 0L;

        Long totalCheckOuts = bookingRepository.countTotalCheckOuts(hotelId);
        if (totalCheckOuts == null) totalCheckOuts = 0L;

        Double averageRating = calculateAverageRating(hotelId);

        List<DashboardResponse.RevenueByHotel> revenueByHotel = buildRevenueByHotel(hotelId);
        List<DashboardResponse.RevenueTrend> revenueTrend = buildRevenueTrend(hotelId);
        List<DashboardResponse.BookingTrend> bookingTrend = buildBookingTrend(hotelId);
        List<DashboardResponse.BookingStatusDistribution> bookingStatusDistribution = buildBookingStatusDistribution(hotelId);

        return DashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .totalBookings(totalBookings)
                .totalCheckIns(totalCheckIns)
                .totalCheckOuts(totalCheckOuts)
                .averageRating(averageRating)
                .revenueByHotel(revenueByHotel)
                .revenueTrend(revenueTrend)
                .bookingTrend(bookingTrend)
                .bookingStatusDistribution(bookingStatusDistribution)
                .build();
    }

    private Double calculateAverageRating(Long hotelId) {
        if (hotelId != null) {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null && hotel.getStarRating() != null) {
                return hotel.getStarRating().doubleValue();
            }
        } else {
            List<Hotel> hotels = hotelRepository.findAll();
            if (!hotels.isEmpty()) {
                double sum = hotels.stream()
                        .filter(h -> h.getStarRating() != null)
                        .mapToInt(Hotel::getStarRating)
                        .sum();
                long count = hotels.stream()
                        .filter(h -> h.getStarRating() != null)
                        .count();
                return count > 0 ? sum / count : 0.0;
            }
        }
        return 0.0;
    }

    private List<DashboardResponse.RevenueByHotel> buildRevenueByHotel(Long hotelId) {
        List<Object[]> results = billRepository.findRevenueByHotel(hotelId);
        final Map<Long, String> hotelNameMap = new HashMap<>();
        
        if (hotelId == null) {
            List<Hotel> hotels = hotelRepository.findAll();
            hotels.forEach(hotel -> hotelNameMap.put(hotel.getId(), hotel.getName()));
        } else {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null) {
                hotelNameMap.put(hotelId, hotel.getName());
            }
        }

        final Map<Long, String> finalHotelNameMap = hotelNameMap;
        return results.stream().map(row -> {
            Long hotelIdFromResult = ((Number) row[0]).longValue();
            BigDecimal revenue = (BigDecimal) row[1];
            String hotelName = finalHotelNameMap.getOrDefault(hotelIdFromResult, "Hotel " + hotelIdFromResult);
            
            return DashboardResponse.RevenueByHotel.builder()
                    .hotelId(hotelIdFromResult)
                    .hotelName(hotelName)
                    .revenue(revenue != null ? revenue : BigDecimal.ZERO)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<DashboardResponse.RevenueTrend> buildRevenueTrend(Long hotelId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        List<Object[]> results = billRepository.findRevenueTrend(startDate, endDate, hotelId);
        
        return results.stream().map(row -> {
            LocalDate date = null;
            if (row[0] != null) {
                if (row[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) row[0]).toLocalDate();
                } else if (row[0] instanceof LocalDate) {
                    date = (LocalDate) row[0];
                } else if (row[0] instanceof java.sql.Timestamp) {
                    date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
                }
            }
            BigDecimal amount = (BigDecimal) row[1];
            
            return DashboardResponse.RevenueTrend.builder()
                    .date(date != null ? date.format(DATE_FORMATTER) : "")
                    .amount(amount != null ? amount : BigDecimal.ZERO)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<DashboardResponse.BookingTrend> buildBookingTrend(Long hotelId) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        List<Object[]> results = bookingRepository.findBookingTrend(startDate, endDate, hotelId);
        
        return results.stream().map(row -> {
            LocalDate date = null;
            if (row[0] != null) {
                if (row[0] instanceof java.sql.Date) {
                    date = ((java.sql.Date) row[0]).toLocalDate();
                } else if (row[0] instanceof LocalDate) {
                    date = (LocalDate) row[0];
                } else if (row[0] instanceof java.sql.Timestamp) {
                    date = ((java.sql.Timestamp) row[0]).toLocalDateTime().toLocalDate();
                }
            }
            Long count = ((Number) row[1]).longValue();
            
            return DashboardResponse.BookingTrend.builder()
                    .date(date != null ? date.format(DATE_FORMATTER) : "")
                    .count(count != null ? count : 0L)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<DashboardResponse.BookingStatusDistribution> buildBookingStatusDistribution(Long hotelId) {
        List<Object[]> results = bookingRepository.findBookingStatusDistribution(hotelId);
        
        return results.stream().map(row -> {
            String status = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            
            return DashboardResponse.BookingStatusDistribution.builder()
                    .status(status)
                    .count(count != null ? count : 0L)
                    .build();
        }).collect(Collectors.toList());
    }
}

