package com.example.hotelservice.controller;

import com.example.hotelservice.dto.HistogramParam;
import com.example.hotelservice.dto.HotelBriefDto;
import com.example.hotelservice.dto.HotelCreateDto;
import com.example.hotelservice.dto.HotelFullDto;
import com.example.hotelservice.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/property-view")
@RequiredArgsConstructor
@Validated
public class HotelController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String DEFAULT_SORT = "name";

    private final HotelService hotelService;

    @Operation(
            summary = "Get a paginated list of all hotels",
            description = "Returns a paginated list of brief hotel information. " +
                    "Supports pagination (page, size) and sorting (sort). " +
                    "Default sort: by name ascending."
    )
    @GetMapping("/hotels")
    public ResponseEntity<Page<HotelBriefDto>> getAllHotels(
            @ParameterObject
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = DEFAULT_SORT)
            Pageable pageable) {
        return ResponseEntity.ok(hotelService.getAllHotels(pageable));
    }

    @Operation(
            summary = "Get detailed information about a hotel by ID",
            description = "Returns complete information about a specific hotel including amenities, contacts, and arrival times."
    )
    @GetMapping("/hotels/{id}")
    public ResponseEntity<HotelFullDto> getHotelById(
            @PathVariable @Parameter(description = "Hotel ID") Long id) {
        return ResponseEntity.ok(hotelService.getHotelById(id));
    }

    @Operation(
            summary = "Search hotels by filters (paginated)",
            description = "Searches hotels using optional filters: name, brand, city, country, amenities. " +
                    "Results are paginated and support sorting. " +
                    "Default sort: by name ascending."
    )
    @GetMapping("/search")
    public ResponseEntity<Page<HotelBriefDto>> searchHotels(
            @RequestParam(required = false) @Parameter(description = "Hotel name (partial match)") String name,
            @RequestParam(required = false) @Parameter(description = "Hotel brand") String brand,
            @RequestParam(required = false) @Parameter(description = "City name") String city,
            @RequestParam(required = false) @Parameter(description = "Country name") String country,
            @RequestParam(required = false) @Parameter(description = "Set of required amenities") Set<String> amenities,
            @ParameterObject
            @PageableDefault(size = DEFAULT_PAGE_SIZE, sort = DEFAULT_SORT)
            Pageable pageable) {
        return ResponseEntity.ok(hotelService.searchHotels(name, brand, city, country, amenities, pageable));
    }

    @Operation(
            summary = "Create a new hotel",
            description = "Creates a new hotel entry with the provided details. " +
                    "Address, contacts and arrival times are required."
    )
    @PostMapping("/hotels")
    public ResponseEntity<HotelBriefDto> createHotel(
            @Valid @RequestBody @Parameter(description = "Hotel creation request") HotelCreateDto dto) {
        HotelBriefDto created = hotelService.createHotel(dto);
        URI location = URI.create("/property-view/hotels/" + created.id());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(
            summary = "Add amenities to an existing hotel",
            description = "Adds one or more amenities to the specified hotel. " +
                    "Duplicates are automatically ignored (as amenities are a set). " +
                    "Blank or null values are ignored. Maximum 50 amenities per request."
    )
    @PostMapping("/hotels/{id}/amenities")
    public ResponseEntity<Void> addAmenities(
            @PathVariable @Parameter(description = "Hotel ID") Long id,
            @Valid @RequestBody
            @Size(max = 50, message = "Maximum 50 amenities per request")
            @Parameter(description = "Set of amenities to add (non-blank strings, duplicates ignored)")
            Set<@NotBlank @Size(max = 100) String> amenities) {
        hotelService.addAmenities(id, amenities);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get histogram by parameter",
            description = "Returns distribution count grouped by the specified parameter. " +
                    "Supported values: brand, city, country, amenities."
    )
    @GetMapping("/histogram/{param}")
    public ResponseEntity<Map<String, Long>> getHistogram(
            @PathVariable @Parameter(description = "Grouping parameter: brand, city, country or amenities") HistogramParam param) {
        return ResponseEntity.ok(hotelService.getHistogram(param));
    }
}