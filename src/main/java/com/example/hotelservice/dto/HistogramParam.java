package com.example.hotelservice.dto;

/**
 * Enum of valid grouping parameters for the histogram endpoint.
 * Spring MVC converts the path variable string to this enum via
 * {@link com.example.hotelservice.config.HistogramParamConverter} (case-insensitive).
 */
public enum HistogramParam {
    BRAND,
    CITY,
    COUNTRY,
    AMENITIES
}
