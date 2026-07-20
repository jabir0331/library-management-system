package com.library.controller;

import com.library.dto.request.BookEditionRequestDTO;
import com.library.dto.response.BookEditionResponseDTO;
import com.library.service.BookEditionService;
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
@RequestMapping("/api/book-editions")
@RequiredArgsConstructor
@Tag(name = "Book Edition Management", description = "Endpoints for managing book editions (physical copies, ISBN, inventory)")
public class BookEditionController {

    private final BookEditionService bookEditionService;

    // 1. Add edition to existing book
    @PostMapping
    @Operation(
            summary = "Add a new Edition to a Book",
            description = "Creates a new edition for an existing book. Each edition has its own ISBN, " +
                    "publication year, publisher, language, copies, and price. ID auto-generated (ED000001 format)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Edition added successfully",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - ISBN already exists, Book not found, or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"ISBN already exists: 9780747532699\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Book not found with id: BK999999\"}")
                    )
            )
    })
    public ResponseEntity<BookEditionResponseDTO> addEdition(
            @Parameter(description = "Edition details to add", required = true)
            @Valid @RequestBody BookEditionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookEditionService.addEdition(request));
    }

    // 2. Get edition by ID
    @GetMapping("/{editionId}")
    @Operation(
            summary = "Get Edition by ID",
            description = "Retrieves edition details by its unique ID (e.g., ED000001)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Edition found",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Edition not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookEdition not found with id: ED999999\"}")
                    )
            )
    })
    public ResponseEntity<BookEditionResponseDTO> getEditionById(
            @Parameter(description = "Edition ID (e.g., ED000001)", required = true, example = "ED000001")
            @PathVariable String editionId) {
        return ResponseEntity.ok(bookEditionService.getEditionById(editionId));
    }

    // 3. Get all editions of a book
    @GetMapping("/book/{bookId}")
    @Operation(
            summary = "Get All Editions of a Book",
            description = "Retrieves all editions belonging to a specific book (e.g., different years, publishers)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Editions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Book not found with id: BK999999\"}")
                    )
            )
    })
    public ResponseEntity<List<BookEditionResponseDTO>> getEditionsByBookId(
            @Parameter(description = "Book ID (e.g., BK000001)", required = true, example = "BK000001")
            @PathVariable String bookId) {
        return ResponseEntity.ok(bookEditionService.getEditionsByBookId(bookId));
    }

    // 4. Update edition
    @PutMapping("/{editionId}")
    @Operation(
            summary = "Update Edition",
            description = "Updates an existing edition's details (ISBN, year, publisher, language, copies, price)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Edition updated successfully",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - ISBN already taken, Book not found, or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"ISBN already exists: 9780747532699\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Edition or Book not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookEdition not found with id: ED999999\"}")
                    )
            )
    })
    public ResponseEntity<BookEditionResponseDTO> updateEdition(
            @Parameter(description = "Edition ID (e.g., ED000001)", required = true, example = "ED000001")
            @PathVariable String editionId,
            @Parameter(description = "Updated edition details", required = true)
            @Valid @RequestBody BookEditionRequestDTO request) {
        return ResponseEntity.ok(bookEditionService.updateEdition(editionId, request));
    }

    // 5. Delete edition
    @DeleteMapping("/{editionId}")
    @Operation(
            summary = "Delete Edition",
            description = "Permanently deletes an edition by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Edition deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Edition not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookEdition not found with id: ED999999\"}")
                    )
            )
    })
    public ResponseEntity<Void> deleteEdition(
            @Parameter(description = "Edition ID (e.g., ED000001)", required = true, example = "ED000001")
            @PathVariable String editionId) {
        bookEditionService.deleteEdition(editionId);
        return ResponseEntity.noContent().build();
    }

    // 6. Update available copies (inventory management)
    @PatchMapping("/{editionId}/available-copies")
    @Operation(
            summary = "Update Available Copies",
            description = "Manually updates the available copies count for an edition. " +
                    "Used for inventory adjustments."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Available copies updated successfully",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid value - Available copies cannot be negative or exceed total copies",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Available copies cannot exceed total copies: 10\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Edition not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"BookEdition not found with id: ED999999\"}")
                    )
            )
    })
    public ResponseEntity<BookEditionResponseDTO> updateAvailableCopies(
            @Parameter(description = "Edition ID (e.g., ED000001)", required = true, example = "ED000001")
            @PathVariable String editionId,
            @Parameter(description = "New available copies count", required = true, example = "8")
            @RequestParam int availableCopies) {
        return ResponseEntity.ok(bookEditionService.updateAvailableCopies(editionId, availableCopies));
    }

    // 7. Get editions with available copies
    @GetMapping("/available")
    @Operation(
            summary = "Get Editions with Available Copies",
            description = "Retrieves all editions that have at least one available copy (availableCopies > 0)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Editions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookEditionResponseDTO>> getEditionsWithAvailableCopies() {
        return ResponseEntity.ok(bookEditionService.getEditionsWithAvailableCopies());
    }

    // 8. Search editions by ISBN
    @GetMapping("/isbn/{isbn}")
    @Operation(
            summary = "Search Editions by ISBN",
            description = "Searches editions by ISBN (partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Editions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookEditionResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookEditionResponseDTO>> getEditionsByIsbn(
            @Parameter(description = "ISBN to search (full or partial)", required = true, example = "9780747532699")
            @PathVariable String isbn) {
        return ResponseEntity.ok(bookEditionService.getEditionsByIsbn(isbn));
    }
}