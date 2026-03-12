package com.example.hotelservice.dto;

import java.util.Map;

public record ValidationErrorResponse(
        String error,
        Map<String, String> fieldErrors
) {
}