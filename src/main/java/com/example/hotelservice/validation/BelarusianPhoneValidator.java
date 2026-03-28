package com.example.hotelservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class BelarusianPhoneValidator implements ConstraintValidator<ValidBelarusianPhone, String> {

    private static final Pattern NON_DIGIT = Pattern.compile("\\D");

    // Mobile: 375 + operator code (25|29|33|44) + 7 digits = 12 digits total
    private static final Pattern MOBILE = Pattern.compile(
            "375(25|29|33|44)\\d{7}"
    );

    // Landline: 375 + city code + subscriber number
    // Minsk (17) has 7-digit subscriber numbers; regional cities have 6-digit numbers
    private static final Pattern LANDLINE = Pattern.compile(
            "375(" +
            "17\\d{7}|"    +  // Minsk
            "162\\d{6}|"   +  // Brest
            "152\\d{6}|"   +  // Grodno
            "212\\d{6}|"   +  // Vitebsk
            "222\\d{6}|"   +  // Mogilev
            "232\\d{6}|"   +  // Gomel
            "174\\d{6}|"   +  // Baranavichy
            "163\\d{6}|"   +  // Pinsk
            "176\\d{6}"    +  // Zhodino / Barysaw area
            ")"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // @NotBlank handles null/blank separately
        }

        String digits = NON_DIGIT.matcher(value).replaceAll("");

        // Normalize domestic prefix 80XX → 375XX
        if (digits.startsWith("80")) {
            digits = "375" + digits.substring(2);
        }

        return MOBILE.matcher(digits).matches() || LANDLINE.matcher(digits).matches();
    }
}
