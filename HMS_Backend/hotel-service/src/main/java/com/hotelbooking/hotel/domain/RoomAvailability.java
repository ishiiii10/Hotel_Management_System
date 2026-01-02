package com.hotelbooking.hotel.domain;


import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hotelbooking.hotel.enums.AvailabilityStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "room_availability",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = { "room_id", "availability_date" }
        )
    },
    indexes = {
        @Index(name = "idx_room_date", columnList = "room_id, availability_date"),
        @Index(name = "idx_hotel_date", columnList = "hotel_id, availability_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hotel_id", nullable = false)
    private Long hotelId;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "availability_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status;

    /**
     * SYSTEM  -> auto-generated availability
     * MANUAL  -> admin / manager block
     * BOOKING -> future booking service
     */
    @Column(nullable = false, length = 20)
    private String source;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.status == null) {
            this.status = AvailabilityStatus.AVAILABLE;
        }

        if (this.source == null) {
            this.source = "SYSTEM";
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
