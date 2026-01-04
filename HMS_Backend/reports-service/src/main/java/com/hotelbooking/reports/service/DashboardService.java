package com.hotelbooking.reports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotelbooking.reports.dto.DashboardResponse;
import com.hotelbooking.reports.repository.billing.BillRepository;
import com.hotelbooking.reports.repository.booking.BookingRepository;
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

    @Cacheable(value = "dashboard", key = "'manager:' + #hotelId")
    public DashboardResponse getManagerDashboard(Long hotelId) {
        return buildDashboard(hotelId);
    }

    @Cacheable(value = "dashboard", key = "'admin:' + (#hotelId != null ? #hotelId : 'all')")
    public DashboardResponse getAdminDashboard(Long hotelId) {
        return buildDashboard(hotelId); // null for all hotels
    }

    private DashboardResponse buildDashboard(Long hotelId) {
        LocalDate today = LocalDate.now();

        // Metrics
        BigDecimal totalRevenue = billRepository.getTotalRevenue(hotelId);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        BigDecimal todayRevenue = billRepository.getTodayRevenue(today, hotelId);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        Long totalBookings = bookingRepository.countTotalBookings(hotelId);
        if (totalBookings == null) totalBookings = 0L;

        Long todayBookings = bookingRepository.countTodayBookings(today, hotelId);
        if (todayBookings == null) todayBookings = 0L;

        Long checkInsToday = bookingRepository.countCheckInsToday(today, hotelId);
        if (checkInsToday == null) checkInsToday = 0L;

        Long checkOutsToday = bookingRepository.countCheckOutsToday(today, hotelId);
        if (checkOutsToday == null) checkOutsToday = 0L;

        Long availableRoomsToday = 0L;
        if (hotelId != null) {
            availableRoomsToday = roomRepository.countAvailableRooms(hotelId);
            if (availableRoomsToday == null) availableRoomsToday = 0L;
        }

        Double averageRating = 4.4; // TODO: Implement when review service is available

        // Graphs
        List<DashboardResponse.RevenueByHour> revenueByHour = buildRevenueByHour(today, hotelId);
        List<DashboardResponse.BookingsBySource> bookingsBySource = buildBookingsBySource(today, hotelId);

        return DashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .todayRevenue(todayRevenue)
                .totalBookings(totalBookings)
                .todayBookings(todayBookings)
                .checkInsToday(checkInsToday)
                .checkOutsToday(checkOutsToday)
                .availableRoomsToday(availableRoomsToday)
                .averageRating(averageRating)
                .todayRevenueByHour(revenueByHour)
                .todayBookingsBySource(bookingsBySource)
                .build();
    }

    private List<DashboardResponse.RevenueByHour> buildRevenueByHour(LocalDate today, Long hotelId) {
        List<Object[]> results = billRepository.findTodayRevenueByHour(today, hotelId);
        Map<Integer, BigDecimal> hourMap = new HashMap<>();
        
        for (Object[] row : results) {
            Integer hour = ((Number) row[0]).intValue();
            BigDecimal amount = (BigDecimal) row[1];
            hourMap.put(hour, amount);
        }

        List<DashboardResponse.RevenueByHour> revenueByHour = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            BigDecimal amount = hourMap.getOrDefault(hour, BigDecimal.ZERO);
            revenueByHour.add(DashboardResponse.RevenueByHour.builder()
                    .hour(hour)
                    .amount(amount)
                    .build());
        }
        return revenueByHour;
    }

    private List<DashboardResponse.BookingsBySource> buildBookingsBySource(LocalDate today, Long hotelId) {
        List<Object[]> results = bookingRepository.findTodayBookingsBySourceAndHour(today, hotelId);
        Map<Integer, Map<String, Long>> hourSourceMap = new HashMap<>();

        for (Object[] row : results) {
            Integer hour = ((Number) row[0]).intValue();
            String source = (String) row[1];
            Long count = ((Number) row[2]).longValue();
            
            hourSourceMap.computeIfAbsent(hour, k -> new HashMap<>()).put(source, count);
        }

        List<DashboardResponse.BookingsBySource> bookingsBySource = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            Map<String, Long> sourceMap = hourSourceMap.getOrDefault(hour, new HashMap<>());
            bookingsBySource.add(DashboardResponse.BookingsBySource.builder()
                    .hour(hour)
                    .publicBookings(sourceMap.getOrDefault("PUBLIC", 0L))
                    .walkInBookings(sourceMap.getOrDefault("WALK_IN", 0L))
                    .build());
        }
        return bookingsBySource;
    }
}

