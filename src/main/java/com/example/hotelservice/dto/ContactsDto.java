package com.example.hotelservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactsDto(
        @NotBlank @Pattern(regexp = "^\\+?[0-9\\s\\-()]{10,20}$") String phone,
        @NotBlank @Email String email) {
}
