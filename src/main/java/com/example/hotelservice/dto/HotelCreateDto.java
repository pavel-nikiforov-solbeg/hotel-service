package com.example.hotelservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record HotelCreateDto(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 50) String brand,
        @Valid @NotNull AddressDto address,
        @Valid @NotNull ContactsDto contacts,
        @Valid @NotNull ArrivalTimeDto arrivalTime) {
}
