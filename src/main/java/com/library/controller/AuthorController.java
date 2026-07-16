package com.library.controller;

import com.library.dto.request.AuthorRequestDTO;
import com.library.dto.response.AuthorResponseDTO;
import com.library.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService authorService;

    // Create Author
    @PostMapping
    public ResponseEntity<AuthorResponseDTO> createAuthor(@Valid @RequestBody AuthorRequestDTO request) {
        AuthorResponseDTO response = authorService.createAuthor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Author by ID (AUTH000001)
    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorResponseDTO> getAuthorById(@PathVariable String authorId) {
        AuthorResponseDTO response = authorService.getAuthorById(authorId);
        return ResponseEntity.ok(response);
    }

    // Get Author by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<AuthorResponseDTO> getAuthorByEmail(@PathVariable String email) {
        AuthorResponseDTO response = authorService.getAuthorByEmail(email);
        return ResponseEntity.ok(response);
    }

    // Get All Authors
    @GetMapping
    public ResponseEntity<List<AuthorResponseDTO>> getAllAuthors() {
        List<AuthorResponseDTO> authors = authorService.getAllAuthors();
        return ResponseEntity.ok(authors);
    }

    // Get Active Authors Only
    @GetMapping("/active")
    public ResponseEntity<List<AuthorResponseDTO>> getActiveAuthors() {
        List<AuthorResponseDTO> authors = authorService.getActiveAuthors();
        return ResponseEntity.ok(authors);
    }

    // Get Inactive Authors Only
    @GetMapping("/inactive")
    public ResponseEntity<List<AuthorResponseDTO>> getInactiveAuthors() {
        List<AuthorResponseDTO> authors = authorService.getInactiveAuthors();
        return ResponseEntity.ok(authors);
    }

    // Update Author
    @PutMapping("/{authorId}")
    public ResponseEntity<AuthorResponseDTO> updateAuthor(
            @PathVariable String authorId,
            @Valid @RequestBody AuthorRequestDTO request) {
        AuthorResponseDTO response = authorService.updateAuthor(authorId, request);
        return ResponseEntity.ok(response);
    }

    // Delete Author (Hard Delete)
    @DeleteMapping("/{authorId}")
    public ResponseEntity<Void> deleteAuthor(@PathVariable String authorId) {
        authorService.deleteAuthor(authorId);
        return ResponseEntity.noContent().build();
    }

    // Activate Author
    @PatchMapping("/{authorId}/activate")
    public ResponseEntity<AuthorResponseDTO> activateAuthor(@PathVariable String authorId) {
        AuthorResponseDTO response = authorService.activateAuthor(authorId);
        return ResponseEntity.ok(response);
    }

    // Deactivate Author
    @PatchMapping("/{authorId}/deactivate")
    public ResponseEntity<AuthorResponseDTO> deactivateAuthor(@PathVariable String authorId) {
        AuthorResponseDTO response = authorService.deactivateAuthor(authorId);
        return ResponseEntity.ok(response);
    }

    // Search Authors
    @GetMapping("/search")
    public ResponseEntity<List<AuthorResponseDTO>> searchAuthors(@RequestParam String keyword) {
        List<AuthorResponseDTO> authors = authorService.searchAuthors(keyword);
        return ResponseEntity.ok(authors);
    }

    // Check if Email Exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = authorService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}