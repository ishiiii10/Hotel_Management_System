package com.hotelbooking.booking.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HotelDetailResponse {
    private Long id;
    private String name;
    private String category; // Jackson will deserialize enum as string
    private String description;
    private String address;
    private String city; // Jackson will deserialize enum as string
    private String state; // Jackson will deserialize enum as string
    private String country;
    private String pincode;
    private String contactNumber;
    private String email;
    private Integer starRating;
    private String amenities;
    private String status; // Jackson will deserialize enum as string
    private Integer totalRooms;
    private Integer availableRooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

