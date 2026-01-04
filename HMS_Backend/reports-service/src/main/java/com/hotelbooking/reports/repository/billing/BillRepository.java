package com.hotelbooking.reports.repository.billing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotelbooking.reports.domain.billing.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b " +
           "WHERE b.status = 'PAID' AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    BigDecimal getTotalRevenue(@Param("hotelId") Long hotelId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b " +
           "WHERE b.status = 'PAID' AND DATE(b.generatedAt) = :today " +
           "AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    BigDecimal getTodayRevenue(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    @Query("SELECT HOUR(b.generatedAt) as hour, SUM(b.totalAmount) as amount " +
           "FROM Bill b WHERE b.status = 'PAID' " +
           "AND DATE(b.generatedAt) = :today AND (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY HOUR(b.generatedAt)")
    List<Object[]> findTodayRevenueByHour(@Param("today") LocalDate today, @Param("hotelId") Long hotelId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b " +
           "WHERE b.status = 'PAID' " +
           "AND YEAR(b.generatedAt) = :year AND MONTH(b.generatedAt) = :month " +
           "AND (:hotelId IS NULL OR b.hotelId = :hotelId)")
    BigDecimal getMonthlyRevenue(@Param("year") int year, @Param("month") int month, @Param("hotelId") Long hotelId);

    @Query("SELECT b.hotelId, COALESCE(SUM(b.totalAmount), 0) as totalAmount " +
           "FROM Bill b WHERE b.status = 'PAID' " +
           "AND (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY b.hotelId")
    List<Object[]> findRevenueByHotel(@Param("hotelId") Long hotelId);

    @Query("SELECT DATE(b.generatedAt) as date, COALESCE(SUM(b.totalAmount), 0) as amount " +
           "FROM Bill b WHERE b.status = 'PAID' " +
           "AND b.generatedAt >= :startDate AND b.generatedAt < :endDate " +
           "AND (:hotelId IS NULL OR b.hotelId = :hotelId) " +
           "GROUP BY DATE(b.generatedAt) " +
           "ORDER BY DATE(b.generatedAt)")
    List<Object[]> findRevenueTrend(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate, 
                                     @Param("hotelId") Long hotelId);
}

