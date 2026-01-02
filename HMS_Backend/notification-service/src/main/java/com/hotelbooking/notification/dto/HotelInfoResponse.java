package com.hotelbooking.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HotelInfoResponse {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String contactNumber;
    private String email;
}

