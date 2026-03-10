package com.example.hotelservice.service;

import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;

import java.util.List;
import java.util.Map;

public interface HotelService {

    List<HotelBriefDto> getAllHotels();

    HotelFullDto getHotelById(Long id);

    List<HotelBriefDto> searchHotels(String name, String brand, String city, String country, List<String> amenities);

    HotelBriefDto createHotel(HotelCreateDto dto);

    void addAmenities(Long id, List<String> amenities);

    Map<String, Long> getHistogram(String param);
}
