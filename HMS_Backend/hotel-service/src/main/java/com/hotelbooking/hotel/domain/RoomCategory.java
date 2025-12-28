package com.hotelbooking.hotel.domain;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Entity
@Table(name = "room_category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 50)
    private String name; // DELUXE, SUITE

    @NotBlank
    @Column(nullable = false, length = 50)
    private String bedType; // KING, QUEEN

    @Column(nullable = false)
    private int maxOccupancy;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column(columnDefinition = "json")
    private String amenities; // JSON string for now

    @Column(nullable = false)
    private Long createdByAdminId;
}