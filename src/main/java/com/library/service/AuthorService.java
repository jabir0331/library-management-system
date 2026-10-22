package com.library.service;

import com.library.dto.request.AuthorRequestDTO;
import com.library.dto.response.AuthorResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuthorService {

    AuthorResponseDTO createAuthor(AuthorRequestDTO request);

    AuthorResponseDTO getAuthorById(String authorId);

    AuthorResponseDTO getAuthorByEmail(String email);

    Page<AuthorResponseDTO> getAllAuthors(Pageable pageable);

    Page<AuthorResponseDTO> getActiveAuthors(Pageable pageable);

    Page<AuthorResponseDTO> getInactiveAuthors(Pageable pageable);

    AuthorResponseDTO updateAuthor(String authorId, AuthorRequestDTO request);

    void deleteAuthor(String authorId);

    AuthorResponseDTO activateAuthor(String authorId);

    AuthorResponseDTO deactivateAuthor(String authorId);

    Page<AuthorResponseDTO> searchAuthors(String keyword, Pageable pageable);

    boolean existsByEmail(String email);
}