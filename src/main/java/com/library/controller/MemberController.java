package com.library.controller;

import com.library.dto.request.MemberRequestDTO;
import com.library.dto.response.MemberResponseDTO;
import com.library.model.Member.MemberStatus;
import com.library.service.MemberService;
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

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Member Management", description = "Endpoints for managing library members (borrowers)")
public class MemberController {

    private final MemberService memberService;

    // 1. Create Member
    @PostMapping
    @Operation(
            summary = "Create a new Member",
            description = "Creates a new library member. Member ID is auto-generated in MEM000001 format."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Member created successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Email already exists or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Email already registered: john@email.com\"}")
                    )
            )
    })
    public ResponseEntity<MemberResponseDTO> createMember(
            @Parameter(description = "Member details to create", required = true)
            @Valid @RequestBody MemberRequestDTO request) {
        MemberResponseDTO response = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Get Member by ID
    @GetMapping("/{memberId}")
    @Operation(
            summary = "Get Member by ID",
            description = "Retrieves member details by their unique ID (e.g., MEM000001)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Member found",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
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
    public ResponseEntity<MemberResponseDTO> getMemberById(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @PathVariable String memberId) {
        MemberResponseDTO response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }

    // 3. Get Member by Email
    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get Member by Email",
            description = "Retrieves member details by their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Member found",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Member not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Member not found with email: non_existent@email.com\"}")
                    )
            )
    })
    public ResponseEntity<MemberResponseDTO> getMemberByEmail(
            @Parameter(description = "Email of the member", required = true, example = "john.doe@email.com")
            @PathVariable String email) {
        MemberResponseDTO response = memberService.getMemberByEmail(email);
        return ResponseEntity.ok(response);
    }

    // 4. Get All Members
    @GetMapping
    @Operation(
            summary = "Get All Members",
            description = "Retrieves a list of all library members"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of members retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            )
    })
    public ResponseEntity<List<MemberResponseDTO>> getAllMembers() {
        List<MemberResponseDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    // 5. Get Members by Status
    @GetMapping("/status/{status}")
    @Operation(
            summary = "Get Members by Status",
            description = "Retrieves members filtered by their status: ACTIVE, SUSPENDED, or INACTIVE"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Members retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Bad Request\",\"message\":\"Invalid status value\"}")
                    )
            )
    })
    public ResponseEntity<List<MemberResponseDTO>> getMembersByStatus(
            @Parameter(description = "Member status: ACTIVE, SUSPENDED, or INACTIVE", required = true, example = "ACTIVE")
            @PathVariable MemberStatus status) {
        List<MemberResponseDTO> members = memberService.getMembersByStatus(status);
        return ResponseEntity.ok(members);
    }

    // 6. Update Member
    @PutMapping("/{memberId}")
    @Operation(
            summary = "Update Member",
            description = "Updates an existing member's details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Member updated successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Email already taken or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Email already registered: john@email.com\"}")
                    )
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
    public ResponseEntity<MemberResponseDTO> updateMember(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @PathVariable String memberId,
            @Parameter(description = "Updated member details", required = true)
            @Valid @RequestBody MemberRequestDTO request) {
        MemberResponseDTO response = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(response);
    }

    // 7. Delete Member
    @DeleteMapping("/{memberId}")
    @Operation(
            summary = "Delete Member",
            description = "Permanently deletes a member by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Member deleted successfully"
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
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @PathVariable String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    // 8. Update Member Status
    @PatchMapping("/{memberId}/status")
    @Operation(
            summary = "Update Member Status",
            description = "Updates a member's status: ACTIVE, SUSPENDED, or INACTIVE"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status updated successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Invalid status: INVALID\"}")
                    )
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
    public ResponseEntity<MemberResponseDTO> updateMemberStatus(
            @Parameter(description = "Member ID (e.g., MEM000001)", required = true, example = "MEM000001")
            @PathVariable String memberId,
            @Parameter(description = "New status: ACTIVE, SUSPENDED, or INACTIVE", required = true, example = "SUSPENDED")
            @RequestParam MemberStatus status) {
        MemberResponseDTO response = memberService.updateMemberStatus(memberId, status);
        return ResponseEntity.ok(response);
    }

    // 9. Search Members
    @GetMapping("/search")
    @Operation(
            summary = "Search Members",
            description = "Searches members by first name, last name, or email (partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MemberResponseDTO.class))
            )
    })
    public ResponseEntity<List<MemberResponseDTO>> searchMembers(
            @Parameter(description = "Search keyword (matches name or email)", required = true, example = "John")
            @RequestParam String keyword) {
        List<MemberResponseDTO> members = memberService.searchMembers(keyword);
        return ResponseEntity.ok(members);
    }

    // 10. Check if Email Exists
    @GetMapping("/exists/email/{email}")
    @Operation(
            summary = "Check if Email Exists",
            description = "Checks if an email is already registered in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns true if email exists, false otherwise",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email to check", required = true, example = "john.doe@email.com")
            @PathVariable String email) {
        boolean exists = memberService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}