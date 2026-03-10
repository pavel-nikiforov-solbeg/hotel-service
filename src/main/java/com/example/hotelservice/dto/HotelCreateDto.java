package com.example.hotelservice.dto;

import jakarta.validation.constraints.NotBlank;

public record HotelCreateDto(
        @NotBlank String name,
        String description,
        @NotBlank String brand,
        AddressDto address,
        ContactsDto contacts,
        ArrivalTimeDto arrivalTime) {
}
