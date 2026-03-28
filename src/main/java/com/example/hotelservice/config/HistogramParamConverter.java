package com.example.hotelservice.config;

import com.example.hotelservice.dto.HistogramParam;
import com.example.hotelservice.exception.InvalidHistogramParameterException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Case-insensitive Spring MVC converter for {@link HistogramParam} path variables.
 * Allows clients to use lowercase values (brand, city, country, amenities) while
 * the enum constants are uppercase. Throws {@link InvalidHistogramParameterException}
 * on unknown values, which is surfaced as a 400 response via {@link com.example.hotelservice.exception.GlobalExceptionHandler}.
 */
@Component
public class HistogramParamConverter implements Converter<String, HistogramParam> {

    @Override
    public HistogramParam convert(String source) {
        try {
            return HistogramParam.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidHistogramParameterException(source);
        }
    }
}
