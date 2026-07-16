package com.library.controller;

import com.library.dto.request.BookRequestDTO;
import com.library.dto.response.BookResponseDTO;
import com.library.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // Create Book
    @PostMapping
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookService.createBook(request));
    }

    // Get Book by ID
    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable String bookId) {
        return ResponseEntity.ok(bookService.getBookById(bookId));
    }

    // Get All Books
    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    // Update Book
    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponseDTO> updateBook(
            @PathVariable String bookId,
            @Valid @RequestBody BookRequestDTO request) {
        return ResponseEntity.ok(bookService.updateBook(bookId, request));
    }

    // Delete Book (only if no editions)
    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

    // Toggle Book Availability
    @PatchMapping("/{bookId}/toggle-availability")
    public ResponseEntity<BookResponseDTO> toggleAvailability(@PathVariable String bookId) {
        return ResponseEntity.ok(bookService.toggleBookAvailability(bookId));
    }

    // Search Books
    @GetMapping("/search")
    public ResponseEntity<List<BookResponseDTO>> searchBooks(@RequestParam String keyword) {
        return ResponseEntity.ok(bookService.searchBooks(keyword));
    }

    // Get Books by Author
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<BookResponseDTO>> getBooksByAuthor(@PathVariable String authorId) {
        return ResponseEntity.ok(bookService.getBooksByAuthor(authorId));
    }

    // Get Books by Genre
    @GetMapping("/genre/{genreId}")
    public ResponseEntity<List<BookResponseDTO>> getBooksByGenre(@PathVariable String genreId) {
        return ResponseEntity.ok(bookService.getBooksByGenre(genreId));
    }

    // Get Available Books
    @GetMapping("/available")
    public ResponseEntity<List<BookResponseDTO>> getAvailableBooks() {
        return ResponseEntity.ok(bookService.getAvailableBooks());
    }
}