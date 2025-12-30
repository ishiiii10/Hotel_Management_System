package com.hotelbooking.hotel.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterPhysicalRoomsRequest {

    @NotNull
    private Long categoryId;

    @NotEmpty
    private List<RoomInput> rooms;

    @Getter
    @Setter
    public static class RoomInput {
        @NotNull
        private String roomNumber;
        private Integer floor;
    }
}