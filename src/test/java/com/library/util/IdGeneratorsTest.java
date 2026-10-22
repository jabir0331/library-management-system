package com.library.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorsTest {

    @Test
    void issueIdStartsAtOneWhenThereIsNoPreviousId() {
        BookIssueIdGenerator generator = new BookIssueIdGenerator();

        assertThat(generator.generateNextId(null)).isEqualTo("BKI000001");
        assertThat(generator.generateNextId("")).isEqualTo("BKI000001");
    }

    @Test
    void issueIdIncrementsAndKeepsPadding() {
        BookIssueIdGenerator generator = new BookIssueIdGenerator();

        assertThat(generator.generateNextId("BKI000009")).isEqualTo("BKI000010");
        assertThat(generator.generateNextId("BKI000999")).isEqualTo("BKI001000");
    }

    @Test
    void issueIdFormatValidation() {
        BookIssueIdGenerator generator = new BookIssueIdGenerator();

        assertThat(generator.isValidFormat("BKI000001")).isTrue();
        assertThat(generator.isValidFormat("BKI1")).isFalse();
        assertThat(generator.isValidFormat("XXX000001")).isFalse();
        assertThat(generator.isValidFormat(null)).isFalse();
    }

    @Test
    void memberIdIncrements() {
        MemberIdGenerator generator = new MemberIdGenerator();

        assertThat(generator.generateNextId(null)).isEqualTo("MEM000001");
        assertThat(generator.generateNextId("MEM000041")).isEqualTo("MEM000042");
    }

    @Test
    void referenceNumbersAreUniqueEvenWithinTheSameSecond() {
        // Regression: references used to be second-resolution and collided
        BookIssueReferenceGenerator generator = new BookIssueReferenceGenerator();
        Set<String> seen = new HashSet<>();

        for (int i = 0; i < 2000; i++) {
            seen.add(generator.generateReferenceNumber());
        }

        assertThat(seen).hasSize(2000);
    }

    @Test
    void referenceNumberFitsTheDatabaseColumn() {
        String reference = new BookIssueReferenceGenerator().generateReferenceNumber();

        assertThat(reference).matches("BKI-\\d{8}-\\d{6}-\\d{4}");
        assertThat(reference.length()).isLessThanOrEqualTo(25);
    }
}
