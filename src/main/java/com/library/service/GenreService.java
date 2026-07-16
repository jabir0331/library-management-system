package com.library.service;

import com.library.dto.request.GenreRequestDTO;
import com.library.dto.response.GenreResponseDTO;

import java.util.List;

public interface GenreService {
    GenreResponseDTO createGenre(GenreRequestDTO request);
    GenreResponseDTO getGenreById(String genreId);
    GenreResponseDTO getGenreByName(String name);
    List<GenreResponseDTO> getAllGenres();
    GenreResponseDTO updateGenre(String genreId, GenreRequestDTO request);
    void deleteGenre(String genreId);
    List<GenreResponseDTO> searchGenres(String keyword);
    boolean existsByName(String name);
}