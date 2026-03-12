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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<HotelBriefDto> getAllHotels(Pageable pageable) {
        Page<Hotel> page = hotelRepository.findAll(pageable);
        Page<HotelBriefDto> result = page.map(hotelMapper::toBriefDto);

        log.info("Returning page {} of hotels, size {}, total elements {}, total pages {}",
                pageable.getPageNumber(), pageable.getPageSize(), result.getTotalElements(), result.getTotalPages());

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public HotelFullDto getHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Hotel not found for id: {}", id);
                    return new HotelNotFoundException(id);
                });

        log.debug("Found hotel: id={}, name={}", id, hotel.getName());
        return hotelMapper.toFullDto(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelBriefDto> searchHotels(String name, String brand, String city, String country,
                                            List<String> amenities, Pageable pageable) {
        var spec = HotelSpecification.buildSpec(name, brand, city, country, amenities);
        Page<Hotel> page = hotelRepository.findAll(spec, pageable);
        Page<HotelBriefDto> result = page.map(hotelMapper::toBriefDto);

        log.info("Hotel search completed - page {}, size {}, found {} matches (total {}), " +
                        "filters: name={}, brand={}, city={}, country={}, amenities={}",
                pageable.getPageNumber(), pageable.getPageSize(), result.getNumberOfElements(), result.getTotalElements(),
                name, brand, city, country, amenities);

        return result;
    }

    @Override
    @Transactional
    public HotelBriefDto createHotel(HotelCreateDto dto) {
        Hotel hotel = hotelMapper.toEntity(dto);
        Hotel saved = hotelRepository.save(hotel);

        log.info("Hotel created successfully - id={}, name={}, brand={}",
                saved.getId(), saved.getName(), saved.getBrand());

        return hotelMapper.toBriefDto(saved);
    }

    @Override
    @Transactional
    public void addAmenities(Long id, Set<String> amenities) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot add amenities - hotel not found: id={}", id);
                    return new HotelNotFoundException(id);
                });

        if (amenities == null || amenities.isEmpty()) {
            log.info("No amenities to add for hotel id={}", id);
            return;
        }

        long addedCount = amenities.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .filter(amenity -> hotel.getAmenities().add(amenity))
                .count();

        log.info("Added {} new amenity/amenities to hotel id={} (from {} provided)",
                addedCount, id, amenities.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getHistogram(String param) {
        if (param == null || param.trim().isEmpty()) {
            log.warn("Histogram requested with empty/missing parameter");
            throw new InvalidHistogramParameterException("parameter is required");
        }

        List<HistogramEntry> entries = switch (param) {
            case "brand" -> hotelRepository.countByBrand();
            case "city" -> hotelRepository.countByCity();
            case "country" -> hotelRepository.countByCountry();
            case "amenities" -> hotelRepository.countByAmenity();
            default -> {
                log.warn("Invalid histogram parameter: {}", param);
                throw new InvalidHistogramParameterException(param);
            }
        };

        Map<String, Long> result = new LinkedHashMap<>();
        for (var entry : entries) {
            result.put(entry.key(), entry.count());
        }

        log.info("Histogram generated for '{}' - {} groups", param, result.size());
        return result;
    }
}