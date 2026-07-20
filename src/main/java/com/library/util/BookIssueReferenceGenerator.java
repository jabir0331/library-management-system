package com.library.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class BookIssueReferenceGenerator {

    private static final String PREFIX = "BKI";
    private static final String SEPARATOR = "-";

    public String generateReferenceNumber() {
        LocalDateTime now = LocalDateTime.now();

        // Format: BKI-YYYYMMDD-HHMMSS
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmss"));

        return PREFIX + SEPARATOR + datePart + SEPARATOR + timePart;
    }
}