package com.library.model;

import com.library.model.enums.IssueStatus;
import com.library.model.enums.ReturnCondition;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "book_issue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookIssue {

    @Id
    @Column(name = "issue_id", length = 10)
    private String issueId;  // BKI000001

    @Column(name = "reference_number", unique = true, nullable = false, length = 25)
    private String referenceNumber;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_edition_id", nullable = false)
    private BookEdition bookEdition;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issued_by", nullable = false)
    private Admin issuedBy;

    @NotNull
    @Column(name = "issued_date", nullable = false)
    private LocalDate issuedDate;

    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    // Return Fields
    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "return_condition", length = 20)
    private ReturnCondition returnCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_by")
    private Admin receivedBy;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "is_fine_paid")
    private boolean isFinePaid = false;

    @Column(name = "lost_book_penalty", precision = 10, scale = 2)
    private BigDecimal lostBookPenalty = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IssueStatus status = IssueStatus.ISSUED;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ═══════════════════════════════════════════════
    // BUSINESS METHODS
    // ═══════════════════════════════════════════════

    public boolean isOverdue() {
        if (returnedDate != null) {
            return returnedDate.isAfter(dueDate);
        }
        return LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        LocalDate dateToCheck = returnedDate != null ? returnedDate : LocalDate.now();
        return ChronoUnit.DAYS.between(dueDate, dateToCheck);
    }

    public boolean isReturned() {
        return returnedDate != null;
    }

    public boolean isLost() {
        return status == IssueStatus.LOST;
    }
}