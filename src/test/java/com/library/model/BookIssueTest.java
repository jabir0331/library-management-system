package com.library.model;

import com.library.model.enums.IssueStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class BookIssueTest {

    private BookIssue issue(IssueStatus status, LocalDate due, LocalDate returned) {
        BookIssue issue = new BookIssue();
        issue.setIssuedDate(LocalDate.now().minusDays(30));
        issue.setDueDate(due);
        issue.setReturnedDate(returned);
        issue.setStatus(status);
        return issue;
    }

    @Test
    void borrowedAndNotYetDueIsIssued() {
        assertThat(issue(IssueStatus.ISSUED, LocalDate.now().plusDays(2), null).getEffectiveStatus())
                .isEqualTo(IssueStatus.ISSUED);
    }

    @Test
    void dueTodayIsStillIssued() {
        assertThat(issue(IssueStatus.ISSUED, LocalDate.now(), null).getEffectiveStatus())
                .isEqualTo(IssueStatus.ISSUED);
    }

    @Test
    void borrowedAndPastDueIsOverdue() {
        BookIssue late = issue(IssueStatus.ISSUED, LocalDate.now().minusDays(1), null);

        assertThat(late.getEffectiveStatus()).isEqualTo(IssueStatus.OVERDUE);
        assertThat(late.getStatus()).isEqualTo(IssueStatus.ISSUED); // never changed in storage
    }

    @Test
    void lateReturnStaysReturned() {
        BookIssue returnedLate = issue(IssueStatus.RETURNED, LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));

        assertThat(returnedLate.getEffectiveStatus()).isEqualTo(IssueStatus.RETURNED);
    }

    @Test
    void legacyOverdueRowThatWasReturnedReadsAsReturned() {
        BookIssue legacy = issue(IssueStatus.OVERDUE, LocalDate.now().minusDays(10), LocalDate.now().minusDays(2));

        assertThat(legacy.getEffectiveStatus()).isEqualTo(IssueStatus.RETURNED);
    }

    @Test
    void lostStaysLost() {
        BookIssue lost = issue(IssueStatus.LOST, LocalDate.now().minusDays(10), LocalDate.now());

        assertThat(lost.getEffectiveStatus()).isEqualTo(IssueStatus.LOST);
    }

    @Test
    void daysOverdueIsNeverNegative() {
        assertThat(issue(IssueStatus.ISSUED, LocalDate.now().plusDays(5), null).getDaysOverdue()).isZero();
        assertThat(issue(IssueStatus.ISSUED, LocalDate.now().minusDays(4), null).getDaysOverdue()).isEqualTo(4);
    }
}
