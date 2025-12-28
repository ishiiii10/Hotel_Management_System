package com.hotelbooking.hotel.dto;

import com.hotelbooking.hotel.domain.City;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBranchRequest {

    @NotNull
    private Long brandId;

    @NotBlank
    private String name;

    @NotNull
    private City city;
}