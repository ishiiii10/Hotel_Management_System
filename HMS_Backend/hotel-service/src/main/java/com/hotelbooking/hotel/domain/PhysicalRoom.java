package com.hotelbooking.hotel.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "physical_rooms",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hotelId", "roomNumber"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhysicalRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, length = 20)
    private String roomNumber;

    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomState state;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.state = RoomState.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}