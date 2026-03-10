package com.example.hotelservice.service;

import com.example.hotelservice.dto.HistogramEntry;
import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.exception.HotelNotFoundException;
import com.example.hotelservice.exception.InvalidHistogramParameterException;
import com.example.hotelservice.mapper.HotelMapper;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.repository.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Override
    public List<HotelBriefDto> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toBriefDto)
                .toList();
    }

    @Override
    public HotelFullDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        return hotelMapper.toFullDto(hotel);
    }

    @Override
    public List<HotelBriefDto> searchHotels(String name, String brand, String city, String country, List<String> amenities) {
        return hotelRepository.findAll(HotelSpecification.buildSpec(name, brand, city, country, amenities))
                .stream()
                .map(hotelMapper::toBriefDto)
                .toList();
    }

    @Override
    @Transactional
    public HotelBriefDto createHotel(HotelCreateDto dto) {
        Hotel hotel = hotelMapper.toEntity(dto);
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toBriefDto(saved);
    }

    @Override
    @Transactional
    public void addAmenities(Long id, List<String> amenities) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));
        hotel.getAmenities().addAll(amenities);
    }

    @Override
    public Map<String, Long> getHistogram(String param) {
        List<HistogramEntry> entries = switch (param) {
            case "brand" -> hotelRepository.countByBrand();
            case "city" -> hotelRepository.countByCity();
            case "country" -> hotelRepository.countByCountry();
            case "amenities" -> hotelRepository.countByAmenity();
            default -> throw new InvalidHistogramParameterException(param);
        };

        Map<String, Long> result = new LinkedHashMap<>();
        for (var entry : entries) {
            result.put(entry.key(), entry.count());
        }
        return result;
    }
}