package com.library.service;

import com.library.dto.request.BookIssueRequestDTO;
import com.library.dto.request.BookReturnRequestDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.model.enums.IssueStatus;
import com.library.model.BookIssue;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;

public interface BookIssueService {

    // Issue a book
    BookIssueResponseDTO issueBook(BookIssueRequestDTO request);

    // Return a book
    BookIssueResponseDTO returnBook(BookReturnRequestDTO request);

    // Mark book as lost
    BookIssueResponseDTO markBookAsLost(String issueId, String adminId, BigDecimal penaltyAmount);

    // Get issue by ID
    BookIssueResponseDTO getIssueById(String issueId);

    // Get issue by reference number
    BookIssueResponseDTO getIssueByReferenceNumber(String referenceNumber);

    // Get all issues
    List<BookIssueResponseDTO> getAllIssues();

    // Get issues by member
    List<BookIssueResponseDTO> getIssuesByMember(String memberId);

    // Get active issues by member
    List<BookIssueResponseDTO> getActiveIssuesByMember(String memberId);

    // Get overdue issues
    List<BookIssueResponseDTO> getOverdueIssues();

    // Get issues by status
    List<BookIssueResponseDTO> getIssuesByStatus(IssueStatus status);

    // Check if member can issue
    boolean canIssueBook(String memberId, String bookEditionId);

    // Calculate fine
    BigDecimal calculateFine(BookIssue issue, LocalDate returnDate);

    // Calculate lost book penalty
    BigDecimal calculateLostBookPenalty(BookIssue issue);
}