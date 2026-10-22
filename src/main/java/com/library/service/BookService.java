package com.library.service;

import com.library.dto.request.BookRequestDTO;
import com.library.dto.response.BookResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookResponseDTO createBook(BookRequestDTO request);
    BookResponseDTO getBookById(String bookId);
    Page<BookResponseDTO> getAllBooks(Pageable pageable);
    BookResponseDTO updateBook(String bookId, BookRequestDTO request);
    void deleteBook(String bookId);

    BookResponseDTO toggleBookAvailability(String bookId);
    Page<BookResponseDTO> searchBooks(String keyword, Pageable pageable);
    Page<BookResponseDTO> getBooksByAuthor(String authorId, Pageable pageable);
    Page<BookResponseDTO> getBooksByGenre(String genreId, Pageable pageable);
    Page<BookResponseDTO> getAvailableBooks(Pageable pageable);
}