package com.library.controller;

import com.library.dto.request.AuthorRequestDTO;
import com.library.dto.response.AuthorResponseDTO;
import com.library.service.AuthorService;
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
@RequestMapping("/api/authors")
@RequiredArgsConstructor
@Tag(name = "Author Management", description = "Endpoints for managing book authors")
public class AuthorController {

    private final AuthorService authorService;

    // 1. Create Author
    @PostMapping
    @Operation(
            summary = "Create a new Author",
            description = "Creates a new author with auto-generated ID (AUTH000001 format)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Author created successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Email already exists or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Email already registered: jk@rowling.com\"}")
                    )
            )
    })
    public ResponseEntity<AuthorResponseDTO> createAuthor(
            @Parameter(description = "Author details to create", required = true)
            @Valid @RequestBody AuthorRequestDTO request) {
        AuthorResponseDTO response = authorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Get Author by ID
    @GetMapping("/{authorId}")
    @Operation(
            summary = "Get Author by ID",
            description = "Retrieves author details by their unique ID (e.g., AUTH000001)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Author not found with id: AUTH999999\"}")
                    )
            )
    })
    public ResponseEntity<AuthorResponseDTO> getAuthorById(
            @Parameter(description = "Author ID (e.g., AUTH000001)", required = true, example = "AUTH000001")
            @PathVariable String authorId) {
        AuthorResponseDTO response = authorService.getAuthorById(authorId);
        return ResponseEntity.ok(response);
    }

    // 3. Get Author by Email
    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get Author by Email",
            description = "Retrieves author details by their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Author found",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Author not found with email: non_existent@email.com\"}")
                    )
            )
    })
    public ResponseEntity<AuthorResponseDTO> getAuthorByEmail(
            @Parameter(description = "Email of the author", required = true, example = "jk@rowling.com")
            @PathVariable String email) {
        AuthorResponseDTO response = authorService.getAuthorByEmail(email);
        return ResponseEntity.ok(response);
    }

    // 4. Get All Authors
    @GetMapping
    @Operation(
            summary = "Get All Authors",
            description = "Retrieves a list of all authors (both active and inactive)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of authors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            )
    })
    public ResponseEntity<List<AuthorResponseDTO>> getAllAuthors() {
        List<AuthorResponseDTO> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    // 5. Get Active Authors Only
    @GetMapping("/active")
    @Operation(
            summary = "Get Active Authors Only",
            description = "Retrieves a list of authors with isActive = true"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of active authors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            )
    })
    public ResponseEntity<List<AuthorResponseDTO>> getActiveAuthors() {
        List<AuthorResponseDTO> authors = authorService.getActiveAuthors();
        return ResponseEntity.ok(authors);
    }

    // 6. Get Inactive Authors Only
    @GetMapping("/inactive")
    @Operation(
            summary = "Get Inactive Authors Only",
            description = "Retrieves a list of authors with isActive = false"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of inactive authors retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            )
    })
    public ResponseEntity<List<AuthorResponseDTO>> getInactiveAuthors() {
        List<AuthorResponseDTO> authors = authorService.getInactiveAuthors();
        return ResponseEntity.ok(authors);
    }

    // 7. Update Author
    @PutMapping("/{authorId}")
    @Operation(
            summary = "Update Author",
            description = "Updates an existing author's details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Author updated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Email already taken or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Email already registered: jk@rowling.com\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Author not found with id: AUTH999999\"}")
                    )
            )
    })
    public ResponseEntity<AuthorResponseDTO> updateAuthor(
            @Parameter(description = "Author ID (e.g., AUTH000001)", required = true, example = "AUTH000001")
            @PathVariable String authorId,
            @Parameter(description = "Updated author details", required = true)
            @Valid @RequestBody AuthorRequestDTO request) {
        AuthorResponseDTO response = authorService.updateAuthor(authorId, request);
        return ResponseEntity.ok(response);
    }

    // 8. Delete Author (Hard Delete)
    @DeleteMapping("/{authorId}")
    @Operation(
            summary = "Delete Author",
            description = "Permanently deletes an author by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Author deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Author not found with id: AUTH999999\"}")
                    )
            )
    })
    public ResponseEntity<Void> deleteAuthor(
            @Parameter(description = "Author ID (e.g., AUTH000001)", required = true, example = "AUTH000001")
            @PathVariable String authorId) {
        authorService.deleteAuthor(authorId);
        return ResponseEntity.noContent().build();
    }

    // 9. Activate Author
    @PatchMapping("/{authorId}/activate")
    @Operation(
            summary = "Activate Author",
            description = "Sets author status to active (isActive = true)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Author activated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Author not found with id: AUTH999999\"}")
                    )
            )
    })
    public ResponseEntity<AuthorResponseDTO> activateAuthor(
            @Parameter(description = "Author ID (e.g., AUTH000001)", required = true, example = "AUTH000001")
            @PathVariable String authorId) {
        AuthorResponseDTO response = authorService.activateAuthor(authorId);
        return ResponseEntity.ok(response);
    }

    // 10. Deactivate Author
    @PatchMapping("/{authorId}/deactivate")
    @Operation(
            summary = "Deactivate Author",
            description = "Sets author status to inactive (isActive = false)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Author deactivated successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Author not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Author not found with id: AUTH999999\"}")
                    )
            )
    })
    public ResponseEntity<AuthorResponseDTO> deactivateAuthor(
            @Parameter(description = "Author ID (e.g., AUTH000001)", required = true, example = "AUTH000001")
            @PathVariable String authorId) {
        AuthorResponseDTO response = authorService.deactivateAuthor(authorId);
        return ResponseEntity.ok(response);
    }

    // 11. Search Authors
    @GetMapping("/search")
    @Operation(
            summary = "Search Authors",
            description = "Searches authors by first name, last name, or email (partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AuthorResponseDTO.class))
            )
    })
    public ResponseEntity<List<AuthorResponseDTO>> searchAuthors(
            @Parameter(description = "Search keyword (matches name or email)", required = true, example = "Rowling")
            @RequestParam String keyword) {
        List<AuthorResponseDTO> authors = authorService.searchAuthors(keyword);
        return ResponseEntity.ok(authors);
    }

    // 12. Check if Email Exists
    @GetMapping("/exists/email/{email}")
    @Operation(
            summary = "Check if Email Exists",
            description = "Checks if an email is already registered for an author"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns true if email exists, false otherwise",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email to check", required = true, example = "jk@rowling.com")
            @PathVariable String email) {
        boolean exists = authorService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}