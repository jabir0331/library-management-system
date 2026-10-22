package com.library.service;

import com.library.dto.request.BookIssueRequestDTO;
import com.library.dto.request.BookReturnRequestDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.model.BookIssue;
import com.library.model.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BookIssueService {

    // Issue a book
    BookIssueResponseDTO issueBook(BookIssueRequestDTO request, String issuedById);

    // Return a book
    BookIssueResponseDTO returnBook(BookReturnRequestDTO request, String receivedById);

    // Mark book as lost
    BookIssueResponseDTO markBookAsLost(String issueId, String adminId, BigDecimal penaltyAmount);

    // Get issue by ID
    BookIssueResponseDTO getIssueById(String issueId);

    // Get issue by reference number
    BookIssueResponseDTO getIssueByReferenceNumber(String referenceNumber);

    // Get all issues
    Page<BookIssueResponseDTO> getAllIssues(Pageable pageable);

    // Get issues by member
    Page<BookIssueResponseDTO> getIssuesByMember(String memberId, Pageable pageable);

    // Get active issues by member
    Page<BookIssueResponseDTO> getActiveIssuesByMember(String memberId, Pageable pageable);

    // Get overdue issues
    Page<BookIssueResponseDTO> getOverdueIssues(Pageable pageable);

    // Get issues by status
    Page<BookIssueResponseDTO> getIssuesByStatus(IssueStatus status, Pageable pageable);

    // Check if member can issue
    boolean canIssueBook(String memberId, String bookEditionId);

    // Calculate fine
    BigDecimal calculateFine(BookIssue issue, LocalDate returnDate);

    // Calculate lost book penalty
    BigDecimal calculateLostBookPenalty(BookIssue issue);
}