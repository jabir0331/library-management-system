package com.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════
    // RESOURCE NOT FOUND - 404
    // ═══════════════════════════════════════════════
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Not Found",
                ex.getMessage()
        );
    }

    // ═══════════════════════════════════════════════
    // BUSINESS LOGIC VIOLATIONS - 400
    // ═══════════════════════════════════════════════
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Business Rule Violation",
                ex.getMessage()
        );
    }

    // ═══════════════════════════════════════════════
    // VALIDATION ERRORS - 400
    // ═══════════════════════════════════════════════
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Error",
                ex.getMessage()
        );
    }

    // ═══════════════════════════════════════════════
    // METHOD ARGUMENT VALIDATION - 400
    // ═══════════════════════════════════════════════
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            validationErrors.put(error.getField(), error.getDefaultMessage());
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Invalid request payload");
        response.put("details", validationErrors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // ═══════════════════════════════════════════════
    // ILLEGAL ARGUMENT / STATE - 400
    // ═══════════════════════════════════════════════
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, Object>> handleIllegalExceptions(RuntimeException ex) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage()
        );
    }

    // ═══════════════════════════════════════════════
    // ALL OTHER UNEXPECTED ERRORS - 500
    // ═══════════════════════════════════════════════
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            WebRequest request) {

        // Log the error for debugging
        ex.printStackTrace();

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please contact support."
        );
    }

    // ═══════════════════════════════════════════════
    // HELPER METHOD TO BUILD RESPONSE
    // ═══════════════════════════════════════════════
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String error,
            String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);

        return new ResponseEntity<>(response, status);
    }
}