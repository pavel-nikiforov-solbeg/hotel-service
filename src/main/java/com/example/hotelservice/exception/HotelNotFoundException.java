package com.example.hotelservice.exception;

public class HotelNotFoundException extends RuntimeException {

    private final Long id;

    public HotelNotFoundException(Long id) {
        super("Hotel not found with id: " + id);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
