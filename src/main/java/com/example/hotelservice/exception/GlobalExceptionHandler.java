package com.example.hotelservice.exception;

import com.example.hotelservice.dto.ErrorResponse;
import com.example.hotelservice.dto.HistogramParam;
import com.example.hotelservice.dto.ValidationErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleHotelNotFound(HotelNotFoundException ex) {
        ErrorResponse response = new ErrorResponse(
                "Hotel not found",
                Map.of("id", ex.getId())
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles path/query variable type mismatches, including unknown {@link HistogramParam} values
     * that fail conversion in {@link com.example.hotelservice.config.HistogramParamConverter}.
     * The {@link InvalidHistogramParameterException} is always wrapped by Spring in
     * {@link org.springframework.core.convert.ConversionFailedException} →
     * {@link MethodArgumentTypeMismatchException}, so it is unwrapped here directly.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof ConversionFailedException cfe && cfe.getCause() instanceof InvalidHistogramParameterException ihpe) {
            ErrorResponse response = new ErrorResponse(
                    ihpe.getMessage(),
                    Map.of("param", ihpe.getParam())
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        String paramValue = String.valueOf(ex.getValue());
        Class<?> requiredType = ex.getRequiredType();
        String message;
        if (requiredType != null && requiredType.isEnum()) {
            String validValues = Arrays.stream(requiredType.getEnumConstants())
                    .map(c -> ((Enum<?>) c).name().toLowerCase())
                    .collect(Collectors.joining(", "));
            message = "Invalid value '" + paramValue + "' for parameter '" + ex.getName()
                    + "'. Accepted: " + validValues;
        } else {
            message = "Invalid value '" + paramValue + "' for parameter '" + ex.getName() + "'";
        }
        return ResponseEntity.badRequest().body(new ErrorResponse(message, Map.of("param", paramValue)));
    }

    /**
     * Handles {@code @Validated} method-parameter constraint violations (e.g. {@code @Size} on
     * the amenities {@code Set} in the controller).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        cv -> {
                            String path = cv.getPropertyPath().toString();
                            int dot = path.lastIndexOf('.');
                            return dot >= 0 ? path.substring(dot + 1) : path;
                        },
                        ConstraintViolation::getMessage,
                        (a, b) -> a
                ));
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse("Validation failed", fieldErrors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ValidationErrorResponse("Validation failed", fieldErrors));
    }

    /**
     * Handles malformed JSON request bodies.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Malformed request body: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("Malformed or missing request body", Map.of()));
    }

    /**
     * Catch-all for unexpected exceptions. Logs the full stack trace server-side
     * and returns a generic 500 without exposing internal details to the client.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("An unexpected error occurred. Please try again later.", Map.of()));
    }
}