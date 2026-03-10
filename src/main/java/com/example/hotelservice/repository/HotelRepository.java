package com.example.hotelservice.repository;

import com.example.hotelservice.dto.HistogramEntry;
import com.example.hotelservice.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    @Query("SELECT new com.example.hotelservice.dto.HistogramEntry(h.brand, COUNT(h)) FROM Hotel h GROUP BY h.brand")
    List<HistogramEntry> countByBrand();

    @Query("SELECT new com.example.hotelservice.dto.HistogramEntry(h.address.city, COUNT(h)) FROM Hotel h GROUP BY h.address.city")
    List<HistogramEntry> countByCity();

    @Query("SELECT new com.example.hotelservice.dto.HistogramEntry(h.address.country, COUNT(h)) FROM Hotel h GROUP BY h.address.country")
    List<HistogramEntry> countByCountry();

    @Query("SELECT new com.example.hotelservice.dto.HistogramEntry(a, COUNT(h)) FROM Hotel h JOIN h.amenities a GROUP BY a")
    List<HistogramEntry> countByAmenity();
}