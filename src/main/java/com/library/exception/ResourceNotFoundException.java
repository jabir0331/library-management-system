package com.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    // For custom messages
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // For entities with Long ID (Admin, Author, etc.)
    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }

    // For entities with String ID (Member, etc.)
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id));
    }

    // For entities with custom field search
    public ResourceNotFoundException(String resource, String field, String value) {
        super(String.format("%s not found with %s: %s", resource, field, value));
    }
}