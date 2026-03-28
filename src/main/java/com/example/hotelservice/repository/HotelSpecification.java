package com.example.hotelservice.repository;

import com.example.hotelservice.entity.Hotel;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class HotelSpecification {

    private static final String LIKE_ESCAPE = "\\";

    private HotelSpecification() {}

    public static Specification<Hotel> hasName(String name) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), likePattern(name), cb.literal(LIKE_ESCAPE.charAt(0)));
    }

    public static Specification<Hotel> hasBrand(String brand) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("brand")), likePattern(brand), cb.literal(LIKE_ESCAPE.charAt(0)));
    }

    public static Specification<Hotel> hasCity(String city) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("address").get("city")), likePattern(city), cb.literal(LIKE_ESCAPE.charAt(0)));
    }

    public static Specification<Hotel> hasCountry(String country) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("address").get("country")), likePattern(country), cb.literal(LIKE_ESCAPE.charAt(0)));
    }

    public static Specification<Hotel> hasAmenity(String amenity) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            Join<Hotel, String> amenitiesJoin = root.join("amenities");
            return cb.like(cb.lower(amenitiesJoin.as(String.class)), likePattern(amenity), cb.literal(LIKE_ESCAPE.charAt(0)));
        };
    }

    public static Specification<Hotel> buildSpec(String name, String brand, String city, String country, Set<String> amenities) {
        List<Specification<Hotel>> specs = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            specs.add(hasName(name));
        }
        if (brand != null && !brand.isBlank()) {
            specs.add(hasBrand(brand));
        }
        if (city != null && !city.isBlank()) {
            specs.add(hasCity(city));
        }
        if (country != null && !country.isBlank()) {
            specs.add(hasCountry(country));
        }
        if (amenities != null) {
            for (String amenity : amenities) {
                if (amenity != null && !amenity.isBlank()) {
                    specs.add(hasAmenity(amenity));
                }
            }
        }

        return Specification.allOf(specs);
    }

    private static String likePattern(String value) {
        return "%" + escapeLike(value.toLowerCase()) + "%";
    }

    private static String escapeLike(String value) {
        return value.replace(LIKE_ESCAPE, LIKE_ESCAPE + LIKE_ESCAPE)
                .replace("%", LIKE_ESCAPE + "%")
                .replace("_", LIKE_ESCAPE + "_");
    }
}