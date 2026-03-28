package com.example.hotelservice;

import com.example.hotelservice.validation.BelarusianPhoneValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class BelarusianPhoneValidatorTest {

    private BelarusianPhoneValidator validator;

    @BeforeEach
    void setUp() {
        validator = new BelarusianPhoneValidator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+375291234567",          // mobile A1/MTS with + prefix
            "375291234567",           // mobile, no prefix
            "80291234567",            // mobile, domestic 80 prefix
            "+375 (29) 123-45-67",   // mobile, formatted with spaces and parens
            "375-29-123-45-67",      // mobile, formatted with dashes
            "+375441234567",          // mobile, A1 044
            "+375251234567",          // mobile, A1 025
            "+375331234567",          // mobile, MTS 033
            "+375171234567",          // Minsk landline (7-digit subscriber)
            "375171234567",           // Minsk, no prefix
            "+375162123456",          // Brest landline
            "+375152123456",          // Grodno landline
            "+375212123456",          // Vitebsk landline
            "+375222123456",          // Mogilev landline
            "+375232123456",          // Gomel landline
            "+375174123456",          // Baranavichy landline
            "+375163123456",          // Pinsk landline
    })
    void validPhones_passValidation(String phone) {
        assertThat(validator.isValid(phone, null)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+7 999 123 45 67",       // Russian number
            "+1-800-555-0199",        // US number
            "1234567",                // too short
            "+375271234567",          // invalid operator code 027
            "+375170000000000",       // Minsk but too many digits
            "375170000",              // Minsk but too few digits
            "not-a-phone",            // no digits at all after stripping
    })
    void invalidPhones_failValidation(String phone) {
        assertThat(validator.isValid(phone, null)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void blankValues_delegatedToNotBlank(String phone) {
        // Validator returns true for blank/empty; @NotBlank handles those separately
        assertThat(validator.isValid(phone, null)).isTrue();
    }

    @Test
    void nullValue_delegatedToNotBlank() {
        // Validator returns true for null; @NotBlank handles null separately
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
