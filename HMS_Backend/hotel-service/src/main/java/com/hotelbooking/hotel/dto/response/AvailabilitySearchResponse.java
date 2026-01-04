package com.hotelbooking.hotel.dto.response;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailabilitySearchResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long hotelId;
    private int availableRooms;
    private List<Long> availableRoomIds;
}
