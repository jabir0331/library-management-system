package com.library.util;

import org.springframework.stereotype.Component;

@Component
public class BookIssueIdGenerator {

    private static final String PREFIX = "BKI";
    private static final int DIGITS = 6;

    public String generateNextId(String lastId) {
        if (lastId == null || lastId.isEmpty()) {
            return PREFIX + String.format("%0" + DIGITS + "d", 1);
        }

        String numberPart = lastId.substring(PREFIX.length());
        long nextNumber = Long.parseLong(numberPart) + 1;

        return PREFIX + String.format("%0" + DIGITS + "d", nextNumber);
    }

    public boolean isValidFormat(String issueId) {
        if (issueId == null || issueId.length() != (PREFIX.length() + DIGITS)) {
            return false;
        }
        if (!issueId.startsWith(PREFIX)) {
            return false;
        }
        String numberPart = issueId.substring(PREFIX.length());
        try {
            Long.parseLong(numberPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}