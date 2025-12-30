package com.hotelbooking.hotel.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_holds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String holdId; // public ID like HOLD-AB12

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private int rooms;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean released;
}
