package com.example.hotelservice.dto;

public record HotelBriefDto(
        Long id,
        String name,
        String description,
        AddressDto address,
        String phone) {
}
