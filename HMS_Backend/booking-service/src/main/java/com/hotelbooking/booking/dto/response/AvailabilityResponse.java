package com.hotelbooking.booking.dto.response;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilityResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long hotelId;
    private Long totalRooms;
    private Long availableRooms;
    private List<AvailableRoom> availableRoomsList;
}
