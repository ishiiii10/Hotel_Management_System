package com.hotelbooking.booking.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private String bookingCode; // e.g. BOOK-8F21A

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Long primaryGuestUserId; // GUEST userId (or walk-in virtual user)

    @Column(nullable = false)
    private String holdId; // consumed hold reference

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime cancelledAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}