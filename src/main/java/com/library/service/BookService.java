package com.library.service;

import com.library.dto.request.BookRequestDTO;
import com.library.dto.response.BookResponseDTO;

import java.util.List;

public interface BookService {

    BookResponseDTO createBook(BookRequestDTO request);
    BookResponseDTO getBookById(String bookId);
    List<BookResponseDTO> getAllBooks();
    BookResponseDTO updateBook(String bookId, BookRequestDTO request);
    void deleteBook(String bookId);

    BookResponseDTO toggleBookAvailability(String bookId);
    List<BookResponseDTO> searchBooks(String keyword);
    List<BookResponseDTO> getBooksByAuthor(String authorId);
    List<BookResponseDTO> getBooksByGenre(String genreId);
    List<BookResponseDTO> getAvailableBooks();
}