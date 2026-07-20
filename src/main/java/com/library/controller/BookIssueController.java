package com.library.controller;

import com.library.dto.request.BookIssueRequestDTO;
import com.library.dto.request.BookReturnRequestDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.model.enums.IssueStatus;
import com.library.service.BookIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Book Issue Management", description = "Endpoints for managing book circulation (issue/return/lost)")
public class BookIssueController {

    private final BookIssueService bookIssueService;

    // 1. Issue a book
    @PostMapping("/issue")
    @Operation(
            summary = "Issue a Book to a Member",
            description = """
            Issues a book edition to a member.
            - Due date is auto-calculated as issuedDate + MAX_ISSUE_DAYS (from System Config)
            - Or you can provide a custom due date in the request
            - Available copies will be decreased automatically
            - Reference number is auto-generated in BKI-YYYYMMDD-HHMMSS format
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Book issued successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Business rule violation - No copies available, Member limit reached, etc.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "No Copies",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"No copies available for this edition\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Member Limit",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Member has reached maximum book limit: 5\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Due Date",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Validation Error\",\"message\":\"Due date must be after issued date\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member, Book Edition, or Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Member not found with id: MEM999999\"}")
                    )
            )
    })
    public ResponseEntity<BookIssueResponseDTO> issueBook(
            @Parameter(
                    description = "Issue request details",
                    required = true,
                    example = "{\n" +
                            "  \"memberId\": \"MEM000001\",\n" +
                            "  \"bookEditionId\": \"ED000001\",\n" +
                            "  \"issuedById\": \"ADM000001\",\n" +
                            "  \"issuedDate\": \"2026-07-20\",\n" +
                            "  \"dueDate\": \"2026-08-03\"\n" +
                            "}"
            )
            @Valid @RequestBody BookIssueRequestDTO request) {
        BookIssueResponseDTO response = bookIssueService.issueBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Return a book
    @PostMapping("/return")
    @Operation(
            summary = "Return a Book",
            description = """
            Processes the return of a borrowed book.
            - Fine is automatically calculated if overdue (using System Config: FINE_PER_DAY)
            - Available copies are increased
            - Status changes to RETURNED or OVERDUE based on return date
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book returned successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Business rule violation - Book already returned, Already lost, etc.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Already Returned",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Book already returned on: 2026-07-22\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Already Lost",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Book is marked as lost. Cannot return.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Return Date",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Validation Error\",\"message\":\"Return date cannot be before issue date\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book Issue or Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookIssue not found with id: BKI999999\"}")
                    )
            )
    })
    public ResponseEntity<BookIssueResponseDTO> returnBook(
            @Parameter(
                    description = "Return request details",
                    required = true,
                    example = "{\n" +
                            "  \"issueId\": \"BKI000001\",\n" +
                            "  \"returnedDate\": \"2026-07-22\",\n" +
                            "  \"receivedById\": \"ADM000001\",\n" +
                            "  \"returnCondition\": \"GOOD\"\n" +
                            "}"
            )
            @Valid @RequestBody BookReturnRequestDTO request) {
        return ResponseEntity.ok(bookIssueService.returnBook(request));
    }

    // 3. Mark book as lost
    @PatchMapping("/{issueId}/lost")
    @Operation(
            summary = "Mark a Book as Lost",
            description = """
            Marks a borrowed book as lost.
            - Penalty is auto-calculated: (price × LOST_BOOK_MULTIPLIER) + SERVICE_CHARGE
            - Or you can provide a custom penalty amount
            - Total copies and available copies are decreased
            - Status changes to LOST
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book marked as lost successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Business rule violation - Book already returned, Already lost",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Already Returned",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Cannot mark as lost. Book already returned.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Already Lost",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Book already marked as lost.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book Issue or Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookIssue not found with id: BKI999999\"}")
                    )
            )
    })
    public ResponseEntity<BookIssueResponseDTO> markAsLost(
            @Parameter(description = "Issue ID (e.g., BKI000001)", required = true, example = "BKI000001")
            @PathVariable String issueId,
            @Parameter(description = "Admin ID processing the loss (e.g., ADM000001)", required = true, example = "ADM000001")
            @RequestParam String adminId,
            @Parameter(description = "Custom penalty amount (optional, auto-calculated if not provided)", example = "50.00")
            @RequestParam(required = false) BigDecimal penaltyAmount) {
        return ResponseEntity.ok(bookIssueService.markBookAsLost(issueId, adminId, penaltyAmount));
    }

    // 4. Get issue by ID
    @GetMapping("/{issueId}")
    @Operation(
            summary = "Get Book Issue by ID",
            description = "Retrieves issue details by its unique ID (e.g., BKI000001)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Issue found",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Issue not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookIssue not found with id: BKI999999\"}")
                    )
            )
    })
    public ResponseEntity<BookIssueResponseDTO> getIssueById(
            @Parameter(description = "Issue ID (e.g., BKI000001)", required = true, example = "BKI000001")
            @PathVariable String issueId) {
        return ResponseEntity.ok(bookIssueService.getIssueById(issueId));
    }

    // 5. Get issue by reference number
    @GetMapping("/reference/{referenceNumber}")
    @Operation(
            summary = "Get Book Issue by Reference Number",
            description = "Retrieves issue details by its human-readable reference number (e.g., BKI-20260720-103045)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Issue found",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Issue not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookIssue not found with reference: BKI-20260720-999999\"}")
                    )
            )
    })
    public ResponseEntity<BookIssueResponseDTO> getIssueByReference(
            @Parameter(description = "Reference number (e.g., BKI-20260720-103045)", required = true, example = "BKI-20260720-103045")
            @PathVariable String referenceNumber) {
        return ResponseEntity.ok(bookIssueService.getIssueByReferenceNumber(referenceNumber));
    }

    // 6. Get all issues
    @GetMapping
    @Operation(
            summary = "Get All Book Issues",
            description = "Retrieves a list of all book issues (including active, returned, overdue, lost)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Issues retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookIssueResponseDTO>> getAllIssues() {
        return ResponseEntity.ok(bookIssueService.getAllIssues());
    }

    // 7. Get issues by member
    @GetMapping("/member/{memberId}")
    @Operation(
            summary = "Get All Issues by Member",
            description = "Retrieves all book issues (past and present) for a specific member"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Issues retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Member not found with id: MEM999999\"}")
                    )
            )
    })
    public ResponseEntity<List<BookIssueResponseDTO>> getIssuesByMember(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @PathVariable String memberId) {
        return ResponseEntity.ok(bookIssueService.getIssuesByMember(memberId));
    }

    // 8. Get active issues by member
    @GetMapping("/member/{memberId}/active")
    @Operation(
            summary = "Get Active Issues by Member",
            description = "Retrieves currently active (ISSUED) book issues for a specific member"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Active issues retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Member not found with id: MEM999999\"}")
                    )
            )
    })
    public ResponseEntity<List<BookIssueResponseDTO>> getActiveIssuesByMember(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @PathVariable String memberId) {
        return ResponseEntity.ok(bookIssueService.getActiveIssuesByMember(memberId));
    }

    // 9. Get overdue issues
    @GetMapping("/overdue")
    @Operation(
            summary = "Get Overdue Issues",
            description = "Retrieves all book issues that are currently overdue (due date has passed and book not returned)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Overdue issues retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookIssueResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookIssueResponseDTO>> getOverdueIssues() {
        return ResponseEntity.ok(bookIssueService.getOverdueIssues());
    }

    // 10. Check if member can issue
    @GetMapping("/can-issue")
    @Operation(
            summary = "Check if Member Can Issue a Book",
            description = """
            Validates if a member can borrow a specific book edition.
            Checks:
            - Member is ACTIVE
            - Member has not reached MAX_BOOKS_PER_MEMBER limit
            - Book edition has available copies
            """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns true if member can issue, false otherwise",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> canIssueBook(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @RequestParam String memberId,
            @Parameter(description = "Book Edition ID (e.g., ED000001)", required = true, example = "ED000001")
            @RequestParam String bookEditionId) {
        return ResponseEntity.ok(bookIssueService.canIssueBook(memberId, bookEditionId));
    }
}