package com.hotelbooking.hotel.dto.request;



import java.math.BigDecimal;

import com.hotelbooking.hotel.enums.RoomCategory;
import com.hotelbooking.hotel.enums.RoomStatus;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoomRequest {

    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number cannot exceed 20 characters")
    private String roomNumber;

    @NotNull(message = "Room type is required")
    private RoomCategory roomType;

    @NotNull(message = "Price per night is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerNight;

    @NotNull(message = "Max occupancy is required")
    @Min(value = 1, message = "Max occupancy must be at least 1")
    @Max(value = 10, message = "Max occupancy cannot exceed 10")
    private Integer maxOccupancy;

    @Min(value = 0, message = "Floor number cannot be negative")
    private Integer floorNumber;

    private String bedType;

    @Min(value = 1, message = "Room size must be positive")
    private Integer roomSize;

    private String amenities;

    private String description;

    private RoomStatus status;

    private Boolean isActive;
}