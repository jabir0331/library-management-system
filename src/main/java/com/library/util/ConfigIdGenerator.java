package com.library.util;

import org.springframework.stereotype.Component;

@Component
public class ConfigIdGenerator {

    private static final String PREFIX = "CFG";
    private static final int DIGITS = 6;

    public String generateNextId(String lastId) {
        if (lastId == null || lastId.isEmpty()) {
            return PREFIX + String.format("%0" + DIGITS + "d", 1);
        }

        String numberPart = lastId.substring(PREFIX.length());
        long nextNumber = Long.parseLong(numberPart) + 1;

        return PREFIX + String.format("%0" + DIGITS + "d", nextNumber);
    }

    public boolean isValidFormat(String configId) {
        if (configId == null || configId.length() != (PREFIX.length() + DIGITS)) {
            return false;
        }

        if (!configId.startsWith(PREFIX)) {
            return false;
        }

        String numberPart = configId.substring(PREFIX.length());
        try {
            Long.parseLong(numberPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}