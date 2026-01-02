package com.hotelbooking.booking.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoomResponse {
    private Long id;
    
    @JsonProperty("hotelId")
    private Long hotelId;
    
    @JsonProperty("roomNumber")
    private String roomNumber;
    
    @JsonProperty("roomType")
    private String roomType; // Jackson will deserialize enum as string
    
    @JsonProperty("pricePerNight")
    private BigDecimal pricePerNight;
    
    @JsonProperty("maxOccupancy")
    private Integer maxOccupancy;
    
    @JsonProperty("floorNumber")
    private Integer floorNumber;
    
    @JsonProperty("bedType")
    private String bedType;
    
    @JsonProperty("roomSize")
    private Integer roomSize;
    
    @JsonProperty("amenities")
    private String amenities;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("status")
    private String status; // Jackson will deserialize enum as string
    
    @JsonProperty("isActive")
    private Boolean isActive;
}

