package com.library.controller;

import com.library.dto.request.GenreRequestDTO;
import com.library.dto.response.GenreResponseDTO;
import com.library.service.GenreService;
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
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Tag(name = "Genre Management", description = "Endpoints for managing book genres/categories")
public class GenreController {

    private final GenreService genreService;

    // 1. Create Genre
    @PostMapping
    @Operation(
            summary = "Create a new Genre",
            description = "Creates a new genre with auto-generated ID (GEN000001 format)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Genre created successfully",
                    content = @Content(schema = @Schema(implementation = GenreResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Genre name already exists or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Genre already exists: Fiction\"}")
                    )
            )
    })
    public ResponseEntity<GenreResponseDTO> createGenre(
            @Parameter(description = "Genre details to create", required = true)
            @Valid @RequestBody GenreRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.createGenre(request));
    }

    // 2. Get Genre by ID
    @GetMapping("/{genreId}")
    @Operation(
            summary = "Get Genre by ID",
            description = "Retrieves genre details by their unique ID (e.g., GEN000001)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Genre found",
                    content = @Content(schema = @Schema(implementation = GenreResponseDTO.class))
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
    public ResponseEntity<GenreResponseDTO> getGenreById(
            @Parameter(description = "Genre ID (e.g., GEN000001)", required = true, example = "GEN000001")
            @PathVariable String genreId) {
        return ResponseEntity.ok(genreService.getGenreById(genreId));
    }

    // 3. Get Genre by Name
    @GetMapping("/name/{name}")
    @Operation(
            summary = "Get Genre by Name",
            description = "Retrieves genre details by its name (exact match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Genre found",
                    content = @Content(schema = @Schema(implementation = GenreResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Genre not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Genre not found with name: NonExistent\"}")
                    )
            )
    })
    public ResponseEntity<GenreResponseDTO> getGenreByName(
            @Parameter(description = "Genre name", required = true, example = "Fiction")
            @PathVariable String name) {
        return ResponseEntity.ok(genreService.getGenreByName(name));
    }

    // 4. Get All Genres
    @GetMapping
    @Operation(
            summary = "Get All Genres",
            description = "Retrieves a list of all genres"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of genres retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenreResponseDTO.class))
            )
    })
    public ResponseEntity<List<GenreResponseDTO>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    // 5. Update Genre
    @PutMapping("/{genreId}")
    @Operation(
            summary = "Update Genre",
            description = "Updates an existing genre's name"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Genre updated successfully",
                    content = @Content(schema = @Schema(implementation = GenreResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Genre name already taken or validation failed",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Genre already exists: Fiction\"}")
                    )
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
    public ResponseEntity<GenreResponseDTO> updateGenre(
            @Parameter(description = "Genre ID (e.g., GEN000001)", required = true, example = "GEN000001")
            @PathVariable String genreId,
            @Parameter(description = "Updated genre details", required = true)
            @Valid @RequestBody GenreRequestDTO request) {
        return ResponseEntity.ok(genreService.updateGenre(genreId, request));
    }

    // 6. Delete Genre
    @DeleteMapping("/{genreId}")
    @Operation(
            summary = "Delete Genre",
            description = "Permanently deletes a genre by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Genre deleted successfully"
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
    public ResponseEntity<Void> deleteGenre(
            @Parameter(description = "Genre ID (e.g., GEN000001)", required = true, example = "GEN000001")
            @PathVariable String genreId) {
        genreService.deleteGenre(genreId);
        return ResponseEntity.noContent().build();
    }

    // 7. Search Genres
    @GetMapping("/search")
    @Operation(
            summary = "Search Genres",
            description = "Searches genres by name (partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GenreResponseDTO.class))
            )
    })
    public ResponseEntity<List<GenreResponseDTO>> searchGenres(
            @Parameter(description = "Search keyword (matches genre name)", required = true, example = "Fict")
            @RequestParam String keyword) {
        return ResponseEntity.ok(genreService.searchGenres(keyword));
    }

    // 8. Check if Genre Name Exists
    @GetMapping("/exists/name/{name}")
    @Operation(
            summary = "Check if Genre Name Exists",
            description = "Checks if a genre name is already in use"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns true if genre name exists, false otherwise",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> existsByName(
            @Parameter(description = "Genre name to check", required = true, example = "Fiction")
            @PathVariable String name) {
        return ResponseEntity.ok(genreService.existsByName(name));
    }
}