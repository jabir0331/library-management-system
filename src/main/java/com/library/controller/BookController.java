package com.library.controller;

import com.library.dto.request.BookRequestDTO;
import com.library.dto.response.BookResponseDTO;
import com.library.service.BookService;
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
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "Endpoints for managing book master records (blueprint)")
public class BookController {

    private final BookService bookService;

    // 1. Create Book (Blueprint only)
    @PostMapping
    @Operation(
            summary = "Create a new Book",
            description = "Creates a book blueprint with auto-generated ID (BK000001 format). " +
                    "Editions can be added separately using the BookEdition endpoints."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Book created successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Author or Genre not found, or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Author not found with id: AUTH999999\"}")
                    )
            )
    })
    public ResponseEntity<BookResponseDTO> createBook(
            @Parameter(description = "Book details to create (authorId, genreId, title, shelfLocation)", required = true)
            @Valid @RequestBody BookRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    // 2. Get Book by ID
    @GetMapping("/{bookId}")
    @Operation(
            summary = "Get Book by ID",
            description = "Retrieves a book with all its editions and aggregated copy counts"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book found",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
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
    public ResponseEntity<BookResponseDTO> getBookById(
            @Parameter(description = "Book ID (e.g., BK000001)", required = true, example = "BK000001")
            @PathVariable String bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    // 3. Get All Books
    @GetMapping
    @Operation(
            summary = "Get All Books",
            description = "Retrieves a list of all books with their editions"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // 4. Update Book
    @PutMapping("/{bookId}")
    @Operation(
            summary = "Update Book",
            description = "Updates the book blueprint (title, author, genre, shelf location)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Book updated successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Author/Genre not found or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Author not found with id: AUTH999999\"}")
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
    public ResponseEntity<BookResponseDTO> updateBook(
            @Parameter(description = "Book ID (e.g., BK000001)", required = true, example = "BK000001")
            @PathVariable String bookId,
            @Parameter(description = "Updated book details", required = true)
            @Valid @RequestBody BookRequestDTO request) {
        return ResponseEntity.ok(bookService.updateBook(bookId, request));
    }

    // 5. Delete Book (only if no editions)
    @DeleteMapping("/{bookId}")
    @Operation(
            summary = "Delete Book",
            description = "Permanently deletes a book. Will fail if the book has existing editions."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Book deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Cannot delete - Book has existing editions",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Cannot delete book with existing editions. Delete editions first.\"}")
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
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID (e.g., BK000001)", required = true, example = "BK000001")
            @PathVariable String bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    // 6. Toggle Book Availability
    @PatchMapping("/{bookId}/toggle-availability")
    @Operation(
            summary = "Toggle Book Availability",
            description = "Toggles the isAvailable flag of a book. " +
                    "Use this to make a book unavailable for borrowing without deleting it."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability toggled successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
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
    public ResponseEntity<BookResponseDTO> toggleAvailability(
            @Parameter(description = "Book ID (e.g., BK000001)", required = true, example = "BK000001")
            @PathVariable String bookId) {
        return ResponseEntity.ok(bookService.toggleBookAvailability(bookId));
    }

    // 7. Search Books
    @GetMapping("/search")
    @Operation(
            summary = "Search Books",
            description = "Searches books by title or author name (partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookResponseDTO>> searchBooks(
            @Parameter(description = "Search keyword (matches title or author name)", required = true, example = "Harry")
            @RequestParam String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword));
    }

    // 8. Get Books by Author
    @GetMapping("/author/{authorId}")
    @Operation(
            summary = "Get Books by Author",
            description = "Retrieves all books written by a specific author"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
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
    public ResponseEntity<List<BookResponseDTO>> getBooksByAuthor(
            @Parameter(description = "Author ID (e.g., AUTH000001)", required = true, example = "AUTH000001")
            @PathVariable String authorId) {
        return ResponseEntity.ok(bookService.getBooksByAuthor(authorId));
    }

    // 9. Get Books by Genre
    @GetMapping("/genre/{genreId}")
    @Operation(
            summary = "Get Books by Genre",
            description = "Retrieves all books belonging to a specific genre"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Genre not found with id: GEN999999\"}")
                    )
            )
    })
    public ResponseEntity<List<BookResponseDTO>> getBooksByGenre(
            @Parameter(description = "Genre ID (e.g., GEN000001)", required = true, example = "GEN000001")
            @PathVariable String genreId) {
        return ResponseEntity.ok(bookService.getBooksByGenre(genreId));
    }

    // 10. Get Available Books
    @GetMapping("/available")
    @Operation(
            summary = "Get Available Books",
            description = "Retrieves all books that are currently available for borrowing (isAvailable = true)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Available books retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BookResponseDTO.class))
            )
    })
    public ResponseEntity<List<BookResponseDTO>> getAvailableBooks() {
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }
}