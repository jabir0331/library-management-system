package com.library.util;

import org.springframework.stereotype.Component;

@Component
public class MemberIdGenerator {

    private static final String PREFIX = "MEM";
    private static final int DIGITS = 6;

    /**
     * Generates the next member ID in format MEM000001, MEM000002, and so on.
     *
     * @param lastId The last member ID (Eg: "MEM000003") or null if none exists
     * @return The next member ID
     */
    public String generateNextId(String lastId) {
        // If no members exist yet, start with MEM000001
        if (lastId == null || lastId.isEmpty()) {
            return PREFIX + String.format("%0" + DIGITS + "d", 1);
        }

        // Extract the number part (remove the PREFIX)
        String numberPart = lastId.substring(PREFIX.length());

        // Parse to long, increment by 1
        long nextNumber = Long.parseLong(numberPart) + 1;

        // Format with leading zeros
        return PREFIX + String.format("%0" + DIGITS + "d", nextNumber);
    }

    /**
     * Checks if a member ID is in the correct format
     *
     * @param memberId The member ID to validate
     * @return true if valid format, false otherwise
     */
    public boolean isValidFormat(String memberId) {
        if (memberId == null || memberId.length() != (PREFIX.length() + DIGITS)) {
            return false;
        }

        if (!memberId.startsWith(PREFIX)) {
            return false;
        }

        String numberPart = memberId.substring(PREFIX.length());
        try {
            Long.parseLong(numberPart);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Extracts the numeric part from a member ID
     *
     * @param memberId The member ID (e.g., "MEM000005")
     * @return The numeric part (e.g., 5)
     */
    public long extractNumber(String memberId) {
        if (!isValidFormat(memberId)) {
            throw new IllegalArgumentException("Invalid member ID format: " + memberId);
        }
        String numberPart = memberId.substring(PREFIX.length());
        return Long.parseLong(numberPart);
    }
}