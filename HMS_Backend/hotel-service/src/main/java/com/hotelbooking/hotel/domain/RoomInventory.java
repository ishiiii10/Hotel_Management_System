package com.hotelbooking.hotel.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "room_inventory",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"hotelId", "categoryId"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hotelId;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private int totalRooms;

    @Column(nullable = false)
    private int outOfService;

    public int getAvailableRooms() {
        return totalRooms - outOfService;
    }
}