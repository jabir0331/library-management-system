package com.library.service;

import com.library.dto.request.AuthorRequestDTO;
import com.library.dto.response.AuthorResponseDTO;

import java.util.List;

public interface AuthorService {

    AuthorResponseDTO createAuthor(AuthorRequestDTO request);

    AuthorResponseDTO getAuthorById(String authorId);

    AuthorResponseDTO getAuthorByEmail(String email);

    List<AuthorResponseDTO> getAllAuthors();

    List<AuthorResponseDTO> getActiveAuthors();

    List<AuthorResponseDTO> getInactiveAuthors();

    AuthorResponseDTO updateAuthor(String authorId, AuthorRequestDTO request);

    void deleteAuthor(String authorId);

    AuthorResponseDTO activateAuthor(String authorId);

    AuthorResponseDTO deactivateAuthor(String authorId);

    List<AuthorResponseDTO> searchAuthors(String keyword);

    boolean existsByEmail(String email);
}