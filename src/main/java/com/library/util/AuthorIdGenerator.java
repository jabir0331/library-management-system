package com.library.util;

import org.springframework.stereotype.Component;

@Component
public class AuthorIdGenerator {

    private static final String PREFIX = "AUTH";
    private static final int DIGITS = 6;

    /**
     * Generates the next author ID in format AUTH000001, AUTH000002, and so on.
     *
     * @param lastId The last author ID (Eg: "AUTH000003") or null if none exists
     * @return The next author ID
     */
    public String generateNextId(String lastId) {
        if (lastId == null || lastId.isEmpty()) {
            return PREFIX + String.format("%0" + DIGITS + "d", 1);
        }

        String numberPart = lastId.substring(PREFIX.length());
        long nextNumber = Long.parseLong(numberPart) + 1;

        return PREFIX + String.format("%0" + DIGITS + "d", nextNumber);
    }

    /**
     * Checks if an author ID is in the correct format
     */
    public boolean isValidFormat(String authorId) {
        if (authorId == null || authorId.length() != (PREFIX.length() + DIGITS)) {
            return false;
        }

        if (!authorId.startsWith(PREFIX)) {
            return false;
        }

        String numberPart = authorId.substring(PREFIX.length());
        try {
            Long.parseLong(numberPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}