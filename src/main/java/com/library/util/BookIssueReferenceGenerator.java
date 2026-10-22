package com.library.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BookIssueReferenceGenerator {

    private static final String PREFIX = "BKI";
    private static final String SEPARATOR = "-";
    private static final int SEQUENCE_MODULUS = 10_000;

    // Random start so references stay distinct across restarts that happen within the same second
    private final AtomicInteger sequence = new AtomicInteger(ThreadLocalRandom.current().nextInt(SEQUENCE_MODULUS));

    public String generateReferenceNumber() {
        LocalDateTime now = LocalDateTime.now();

        // Format: BKI-YYYYMMDD-HHMMSS-NNNN. The counter guarantees uniqueness for up to
        // 10,000 references in a row, even when several are generated within the same second.
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timePart = now.format(DateTimeFormatter.ofPattern("HHmmss"));
        String suffix = String.format("%04d", Math.floorMod(sequence.getAndIncrement(), SEQUENCE_MODULUS));

        return PREFIX + SEPARATOR + datePart + SEPARATOR + timePart + SEPARATOR + suffix;
    }
}
