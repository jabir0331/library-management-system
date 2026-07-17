package com.library.service;

import com.library.dto.request.BookEditionRequestDTO;
import com.library.dto.response.BookEditionResponseDTO;

import java.util.List;

public interface BookEditionService {

    BookEditionResponseDTO addEdition(BookEditionRequestDTO request);
    BookEditionResponseDTO getEditionById(String editionId);
    List<BookEditionResponseDTO> getEditionsByBookId(String bookId);
    BookEditionResponseDTO updateEdition(String editionId, BookEditionRequestDTO request);
    void deleteEdition(String editionId);
    BookEditionResponseDTO updateAvailableCopies(String editionId, int newAvailableCopies);
    List<BookEditionResponseDTO> getEditionsWithAvailableCopies();
    List<BookEditionResponseDTO> getEditionsByIsbn(String isbn);
}