package com.hotelbooking.reports.domain.hotel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hotels")
@Getter
@Setter
public class Hotel {

    @Id
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "category")
    private String category;

    @Column(name = "city")
    private String city;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    private String status;

    @Column(name = "star_rating")
    private Integer starRating;
}

