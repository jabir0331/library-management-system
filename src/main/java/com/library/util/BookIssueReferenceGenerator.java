package com.library.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class BookIssueReferenceGenerator {

    private static final String PREFIX = "BKI";
    private static final String SEPARATOR = "-";

    public String generateReferenceNumber() {
        LocalDateTime now = LocalDateTime.now();

        // Format: BKI-YYYYMMDD-HHMMSS-XXXX (random suffix: two issues in the same second must not collide)
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String suffix = UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        return PREFIX + SEPARATOR + datePart + SEPARATOR + timePart + SEPARATOR + suffix;
    }
}