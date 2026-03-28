package com.example.hotelservice.dto;

import com.example.hotelservice.validation.ValidBelarusianPhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactsDto(
        @NotBlank @ValidBelarusianPhone String phone,
        @NotBlank @Email String email) {
}
