package com.library.dto.response;

import com.library.dto.BaseDTO;
import com.library.model.enums.IssueStatus;
import com.library.model.enums.ReturnCondition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookIssueResponseDTO implements BaseDTO {

    private String issueId;
    private String referenceNumber;
    private String memberId;
    private String memberName;
    private String bookEditionId;
    private String bookTitle;
    private String isbn;
    private String issuedBy;
    private LocalDate issuedDate;
    private LocalDate dueDate;
    private LocalDate returnedDate;
    private ReturnCondition returnCondition;
    private String receivedBy;
    private BigDecimal fineAmount;
    private boolean isFinePaid;
    private BigDecimal lostBookPenalty;
    private IssueStatus status;
    private boolean isOverdue;
    private long daysOverdue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}