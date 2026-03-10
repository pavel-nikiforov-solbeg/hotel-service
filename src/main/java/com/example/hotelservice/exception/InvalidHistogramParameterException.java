package com.example.hotelservice.exception;

public class InvalidHistogramParameterException extends RuntimeException {

    private final String param;

    public InvalidHistogramParameterException(String param) {
        super("Unknown histogram parameter: " + param + ". Valid values: brand, city, country, amenities");
        this.param = param;
    }

    public String getParam() {
        return param;
    }
}
