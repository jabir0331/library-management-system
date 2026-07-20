package com.library.service.impl;

import com.library.dto.request.BookRequestDTO;
import com.library.dto.response.BookEditionResponseDTO;
import com.library.dto.response.BookResponseDTO;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Author;
import com.library.model.Book;
import com.library.model.Genre;
import com.library.repository.AuthorRepository;
import com.library.repository.BookEditionRepository;
import com.library.repository.BookRepository;
import com.library.repository.GenreRepository;
import com.library.service.BookService;
import com.library.util.BookIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookEditionRepository bookEditionRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final BookIdGenerator bookIdGenerator;

    @Override
    public BookResponseDTO createBook(BookRequestDTO request) {
        // Validate author exists
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", request.getAuthorId()));

        // Validate genre exists
        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre", request.getGenreId()));

        // Create Book
        Book book = new Book();
        String lastBookId = bookRepository.findLastBookId();
        book.setBookId(bookIdGenerator.generateNextId(lastBookId));
        book.setAuthor(author);
        book.setGenre(genre);
        book.setTitle(request.getTitle());
        book.setShelfLocation(request.getShelfLocation());
        book.setAvailable(true);

        // Save
        Book savedBook = bookRepository.save(book);

        // Return response
        return mapToResponseDTO(savedBook);
    }

    @Override
    public BookResponseDTO getBookById(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        return mapToResponseDTO(book);
    }

    @Override
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponseDTO updateBook(String bookId, BookRequestDTO request) {
        // Find existing book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));

        // Validate author exists
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author", request.getAuthorId()));

        // Validate genre exists
        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new ResourceNotFoundException("Genre", request.getGenreId()));

        // Update book fields
        book.setAuthor(author);
        book.setGenre(genre);
        book.setTitle(request.getTitle());
        book.setShelfLocation(request.getShelfLocation());

        // Save
        Book updatedBook = bookRepository.save(book);

        // Return response
        return mapToResponseDTO(updatedBook);
    }

    @Override
    public void deleteBook(String bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", bookId);
        }

        // Check if book has editions
        if (!bookRepository.findEditionsByBookId(bookId).isEmpty()) {
            throw new BusinessException("Cannot delete book with existing editions. Delete editions first.");
        }

        bookRepository.deleteById(bookId);
    }

    @Override
    public BookResponseDTO toggleBookAvailability(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book", bookId));
        book.setAvailable(!book.isAvailable());
        Book updatedBook = bookRepository.save(book);
        return mapToResponseDTO(updatedBook);
    }

    @Override
    public List<BookResponseDTO> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDTO> getBooksByAuthor(String authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author", authorId);
        }
        return bookRepository.findByAuthor_AuthorId(authorId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDTO> getBooksByGenre(String genreId) {
        if (!genreRepository.existsById(genreId)) {
            throw new ResourceNotFoundException("Genre", genreId);
        }
        return bookRepository.findByGenre_GenreId(genreId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDTO> getAvailableBooks() {
        return bookRepository.findByIsAvailableTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ⭐ MANUAL MAPPING
    private BookResponseDTO mapToResponseDTO(Book book) {
        BookResponseDTO response = new BookResponseDTO();

        // Basic book info
        response.setBookId(book.getBookId());
        response.setTitle(book.getTitle());
        response.setAvailable(book.isAvailable());
        response.setShelfLocation(book.getShelfLocation());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());

        // Author info
        if (book.getAuthor() != null) {
            response.setAuthorId(book.getAuthor().getAuthorId());
            response.setAuthorName(book.getAuthor().getFullName());
        }

        // Genre info
        if (book.getGenre() != null) {
            response.setGenreId(book.getGenre().getGenreId());
            response.setGenreName(book.getGenre().getName());
        }

        // Map editions
        List<BookEditionResponseDTO> editionDTOs = book.getEditions().stream()
                .map(this::mapEditionToResponseDTO)
                .collect(Collectors.toList());
        response.setEditions(editionDTOs);

        return response;
    }

    private BookEditionResponseDTO mapEditionToResponseDTO(com.library.model.BookEdition edition) {
        BookEditionResponseDTO response = new BookEditionResponseDTO();
        response.setEditionId(edition.getEditionId());
        response.setBookId(edition.getBook().getBookId());
        response.setIsbn(edition.getIsbn());
        response.setPublicationYear(edition.getPublicationYear());
        response.setPublisher(edition.getPublisher());
        response.setLanguage(edition.getLanguage());
        response.setTotalCopies(edition.getTotalCopies());
        response.setAvailableCopies(edition.getAvailableCopies());
        response.setPrice(edition.getPrice());
        response.setCreatedAt(edition.getCreatedAt());
        response.setUpdatedAt(edition.getUpdatedAt());
        return response;
    }
}