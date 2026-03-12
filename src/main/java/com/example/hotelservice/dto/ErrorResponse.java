package com.example.hotelservice.dto;

import java.util.Map;

public record ErrorResponse(
        String error,
        Map<String, Object> details
) {
}