package com.hotelbooking.reports.domain.hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {

    @Id
    private Long id;

    @Column(name = "hotel_id")
    private Long hotelId;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "status")
    private String status;
}

