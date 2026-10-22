package com.library.service;

import com.library.dto.request.BookEditionRequestDTO;
import com.library.dto.response.BookEditionResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookEditionService {

    BookEditionResponseDTO addEdition(BookEditionRequestDTO request);
    BookEditionResponseDTO getEditionById(String editionId);
    Page<BookEditionResponseDTO> getEditionsByBookId(String bookId, Pageable pageable);
    BookEditionResponseDTO updateEdition(String editionId, BookEditionRequestDTO request);
    void deleteEdition(String editionId);
    BookEditionResponseDTO updateAvailableCopies(String editionId, int newAvailableCopies);
    Page<BookEditionResponseDTO> getEditionsWithAvailableCopies(Pageable pageable);
    Page<BookEditionResponseDTO> getEditionsByIsbn(String isbn, Pageable pageable);
}