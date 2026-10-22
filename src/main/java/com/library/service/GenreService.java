package com.library.service;

import com.library.dto.request.GenreRequestDTO;
import com.library.dto.response.GenreResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GenreService {
    GenreResponseDTO createGenre(GenreRequestDTO request);
    GenreResponseDTO getGenreById(String genreId);
    GenreResponseDTO getGenreByName(String name);
    Page<GenreResponseDTO> getAllGenres(Pageable pageable);
    GenreResponseDTO updateGenre(String genreId, GenreRequestDTO request);
    void deleteGenre(String genreId);
    Page<GenreResponseDTO> searchGenres(String keyword, Pageable pageable);
    boolean existsByName(String name);
}