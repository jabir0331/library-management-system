package com.library.util;

import org.springframework.stereotype.Component;

@Component
public class GenreIdGenerator {
    private static final String PREFIX = "GEN";
    private static final int DIGITS = 6;

    public String generateNextId(String lastId) {
        if (lastId == null || lastId.isEmpty()) {
            return PREFIX + String.format("%0" + DIGITS + "d", 1);
        }
        String numberPart = lastId.substring(PREFIX.length());
        long nextNumber = Long.parseLong(numberPart) + 1;
        return PREFIX + String.format("%0" + DIGITS + "d", nextNumber);
    }
}