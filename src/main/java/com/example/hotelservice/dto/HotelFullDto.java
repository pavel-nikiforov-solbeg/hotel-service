package com.example.hotelservice.dto;

import java.util.List;

public record HotelFullDto(
        Long id,
        String name,
        String description,
        String brand,
        AddressDto address,
        ContactsDto contacts,
        ArrivalTimeDto arrivalTime,
        List<String> amenities) {
}
