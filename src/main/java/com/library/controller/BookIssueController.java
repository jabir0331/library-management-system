package com.library.controller;

import com.library.dto.request.BookIssueRequestDTO;
import com.library.dto.request.BookReturnRequestDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.model.enums.IssueStatus;
import com.library.service.BookIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/book-issues")
@RequiredArgsConstructor
public class BookIssueController {

    private final BookIssueService bookIssueService;

    // 1. Issue a book
    @PostMapping("/issue")
    public ResponseEntity<BookIssueResponseDTO> issueBook(@Valid @RequestBody BookIssueRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookIssueService.issueBook(request));
    }

    // 2. Return a book
    @PostMapping("/return")
    public ResponseEntity<BookIssueResponseDTO> returnBook(@Valid @RequestBody BookReturnRequestDTO request) {
        return ResponseEntity.ok(bookIssueService.returnBook(request));
    }

    // 3. Mark book as lost
    @PatchMapping("/{issueId}/lost")
    public ResponseEntity<BookIssueResponseDTO> markAsLost(
            @PathVariable String issueId,
            @RequestParam String adminId,
            @RequestParam(required = false) BigDecimal penaltyAmount) {
        return ResponseEntity.ok(bookIssueService.markBookAsLost(issueId, adminId, penaltyAmount));
    }

    // 4. Get issue by ID
    @GetMapping("/{issueId}")
    public ResponseEntity<BookIssueResponseDTO> getIssueById(@PathVariable String issueId) {
        return ResponseEntity.ok(bookIssueService.getIssueById(issueId));
    }

    // 5. Get issue by reference number
    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<BookIssueResponseDTO> getIssueByReference(@PathVariable String referenceNumber) {
        return ResponseEntity.ok(bookIssueService.getIssueByReferenceNumber(referenceNumber));
    }

    // 6. Get all issues
    @GetMapping
    public ResponseEntity<List<BookIssueResponseDTO>> getAllIssues() {
        return ResponseEntity.ok(bookIssueService.getAllIssues());
    }

    // 7. Get issues by member
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<BookIssueResponseDTO>> getIssuesByMember(@PathVariable String memberId) {
        return ResponseEntity.ok(bookIssueService.getIssuesByMember(memberId));
    }

    // 8. Get active issues by member
    @GetMapping("/member/{memberId}/active")
    public ResponseEntity<List<BookIssueResponseDTO>> getActiveIssuesByMember(@PathVariable String memberId) {
        return ResponseEntity.ok(bookIssueService.getActiveIssuesByMember(memberId));
    }

    // 9. Get overdue issues
    @GetMapping("/overdue")
    public ResponseEntity<List<BookIssueResponseDTO>> getOverdueIssues() {
        return ResponseEntity.ok(bookIssueService.getOverdueIssues());
    }

    // 10. Check if member can issue
    @GetMapping("/can-issue")
    public ResponseEntity<Boolean> canIssueBook(
            @RequestParam String memberId,
            @RequestParam String bookEditionId) {
        return ResponseEntity.ok(bookIssueService.canIssueBook(memberId, bookEditionId));
    }
}