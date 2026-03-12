package com.example.hotelservice.service;

import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface HotelService {

    Page<HotelBriefDto> getAllHotels(Pageable pageable);

    HotelFullDto getHotelById(Long id);

    Page<HotelBriefDto> searchHotels(String name, String brand, String city, String country, List<String> amenities, Pageable pageable);

    HotelBriefDto createHotel(HotelCreateDto dto);

    void addAmenities(Long id, Set<String> amenities);

    Map<String, Long> getHistogram(String param);
}