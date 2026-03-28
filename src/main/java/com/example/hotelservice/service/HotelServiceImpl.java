package com.example.hotelservice.service;

import com.example.hotelservice.dto.HistogramEntry;
import com.example.hotelservice.dto.HistogramParam;
import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import com.example.hotelservice.entity.Hotel;
import com.example.hotelservice.exception.HotelNotFoundException;
import com.example.hotelservice.mapper.HotelMapper;
import com.example.hotelservice.repository.HotelRepository;
import com.example.hotelservice.repository.HotelSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    @Override
    public Page<HotelBriefDto> getAllHotels(Pageable pageable) {
        Page<Hotel> page = hotelRepository.findAll(pageable);
        Page<HotelBriefDto> result = page.map(hotelMapper::toBriefDto);

        log.debug("Returning page {} of hotels, size {}, total elements {}, total pages {}",
                pageable.getPageNumber(), pageable.getPageSize(), result.getTotalElements(), result.getTotalPages());

        return result;
    }

    @Override
    @Cacheable(value = "hotels", key = "#id")
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
    public Page<HotelBriefDto> searchHotels(String name, String brand, String city, String country,
                                            Set<String> amenities, Pageable pageable) {
        var spec = HotelSpecification.buildSpec(name, brand, city, country, amenities);
        Page<Hotel> page = hotelRepository.findAll(spec, pageable);
        Page<HotelBriefDto> result = page.map(hotelMapper::toBriefDto);

        log.debug("Hotel search completed - page {}, size {}, found {} matches (total {}), " +
                        "filters: name={}, brand={}, city={}, country={}, amenities={}",
                pageable.getPageNumber(), pageable.getPageSize(), result.getNumberOfElements(), result.getTotalElements(),
                name, brand, city, country, amenities);

        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "histograms", allEntries = true)
    public HotelBriefDto createHotel(HotelCreateDto dto) {
        Hotel hotel = hotelMapper.toEntity(dto);
        Hotel saved = hotelRepository.save(hotel);

        log.info("Hotel created successfully - id={}, name={}, brand={}",
                saved.getId(), saved.getName(), saved.getBrand());

        return hotelMapper.toBriefDto(saved);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "hotels", key = "#id"),
            @CacheEvict(value = "histograms", allEntries = true)
    })
    public void addAmenities(Long id, Set<String> amenities) {
        if (amenities == null || amenities.isEmpty()) {
            log.info("No amenities to add for hotel id={}", id);
            return;
        }

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cannot add amenities - hotel not found: id={}", id);
                    return new HotelNotFoundException(id);
                });

        long addedCount = 0;
        for (String raw : amenities) {
            if (raw == null) continue;
            String amenity = raw.trim().toLowerCase();
            if (!amenity.isBlank() && hotel.getAmenities().add(amenity)) {
                addedCount++;
            }
        }

        log.info("Added {} new amenity/amenities to hotel id={} (from {} provided)",
                addedCount, id, amenities.size());
    }

    @Override
    @Cacheable(value = "histograms", key = "#param")
    public Map<String, Long> getHistogram(HistogramParam param) {
        List<HistogramEntry> entries = switch (param) {
            case BRAND -> hotelRepository.countByBrand();
            case CITY -> hotelRepository.countByCity();
            case COUNTRY -> hotelRepository.countByCountry();
            case AMENITIES -> hotelRepository.countByAmenity();
        };

        Map<String, Long> result = entries.stream()
                .collect(Collectors.toMap(
                        HistogramEntry::key,
                        HistogramEntry::count,
                        (a, b) -> a,
                        LinkedHashMap::new));

        log.debug("Histogram generated for '{}' — {} groups", param, result.size());
        return result;
    }
}