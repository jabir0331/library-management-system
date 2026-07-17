package com.library.controller;

import com.library.dto.request.BookEditionRequestDTO;
import com.library.dto.response.BookEditionResponseDTO;
import com.library.service.BookEditionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/book-editions")
@RequiredArgsConstructor
public class BookEditionController {

    private final BookEditionService bookEditionService;

    // Add edition to existing book
    @PostMapping
    public ResponseEntity<BookEditionResponseDTO> addEdition(@Valid @RequestBody BookEditionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookEditionService.addEdition(request));
    }

    // Get edition by ID
    @GetMapping("/{editionId}")
    public ResponseEntity<BookEditionResponseDTO> getEditionById(@PathVariable String editionId) {
        return ResponseEntity.ok(bookEditionService.getEditionById(editionId));
    }

    // Get all editions of a book
    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<BookEditionResponseDTO>> getEditionsByBookId(@PathVariable String bookId) {
        return ResponseEntity.ok(bookEditionService.getEditionsByBookId(bookId));
    }

    // Update edition
    @PutMapping("/{editionId}")
    public ResponseEntity<BookEditionResponseDTO> updateEdition(
            @PathVariable String editionId,
            @Valid @RequestBody BookEditionRequestDTO request) {
        return ResponseEntity.ok(bookEditionService.updateEdition(editionId, request));
    }

    // Delete edition
    @DeleteMapping("/{editionId}")
    public ResponseEntity<Void> deleteEdition(@PathVariable String editionId) {
        bookEditionService.deleteEdition(editionId);
        return ResponseEntity.noContent().build();
    }

    // Update available copies
    @PatchMapping("/{editionId}/available-copies")
    public ResponseEntity<BookEditionResponseDTO> updateAvailableCopies(
            @PathVariable String editionId,
            @RequestParam int availableCopies) {
        return ResponseEntity.ok(bookEditionService.updateAvailableCopies(editionId, availableCopies));
    }

    // Get editions with available copies
    @GetMapping("/available")
    public ResponseEntity<List<BookEditionResponseDTO>> getEditionsWithAvailableCopies() {
        return ResponseEntity.ok(bookEditionService.getEditionsWithAvailableCopies());
    }

    // Search editions by ISBN
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<List<BookEditionResponseDTO>> getEditionsByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(bookEditionService.getEditionsByIsbn(isbn));
    }
}