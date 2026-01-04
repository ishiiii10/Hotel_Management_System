package com.hotelbooking.booking.dto.response;

import java.io.Serializable;
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
public class HotelDetailResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String category;
    private String description;
    private String address;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String contactNumber;
    private String email;
    private Integer starRating;
    private String amenities;
    private String status;
    private Integer totalRooms;
    private Integer availableRooms;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
