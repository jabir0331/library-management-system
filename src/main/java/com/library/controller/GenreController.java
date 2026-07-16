package com.library.controller;

import com.library.dto.request.GenreRequestDTO;
import com.library.dto.response.GenreResponseDTO;
import com.library.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<GenreResponseDTO> createGenre(@Valid @RequestBody GenreRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.createGenre(request));
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<GenreResponseDTO> getGenreById(@PathVariable String genreId) {
        return ResponseEntity.ok(genreService.getGenreById(genreId));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<GenreResponseDTO> getGenreByName(@PathVariable String name) {
        return ResponseEntity.ok(genreService.getGenreByName(name));
    }

    @GetMapping
    public ResponseEntity<List<GenreResponseDTO>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<GenreResponseDTO> updateGenre(
            @PathVariable String genreId,
            @Valid @RequestBody GenreRequestDTO request) {
        return ResponseEntity.ok(genreService.updateGenre(genreId, request));
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<Void> deleteGenre(@PathVariable String genreId) {
        genreService.deleteGenre(genreId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<GenreResponseDTO>> searchGenres(@RequestParam String keyword) {
        return ResponseEntity.ok(genreService.searchGenres(keyword));
    }

    @GetMapping("/exists/name/{name}")
    public ResponseEntity<Boolean> existsByName(@PathVariable String name) {
        return ResponseEntity.ok(genreService.existsByName(name));
    }
}