package com.example.hotelservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for hotel address.
 *
 * Note: The postcode validation is currently Belarus-specific (exactly 6 digits).
 * If the service expands to support hotels in other countries, this constraint
 * should be relaxed or made configurable (e.g. per-country validation rules).
 */
public record AddressDto(
        @NotBlank @Size(max = 20) String houseNumber,
        @NotBlank @Size(max = 100) String street,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String country,

        // Belarus-specific: exactly 6 digits (e.g. 220004 for Minsk)
        // make configurable or remove strict pattern when supporting other countries
        @NotBlank @Size(min = 6, max = 6) @Pattern(regexp = "^\\d{6}$") String postCode) {
}