package com.library.service.impl;

import com.library.dto.request.BookEditionRequestDTO;
import com.library.dto.response.BookEditionResponseDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.BookEdition;
import com.library.repository.BookEditionRepository;
import com.library.repository.BookRepository;
import com.library.service.BookEditionService;
import com.library.util.BookEditionIdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookEditionServiceImpl implements BookEditionService {

    private final BookEditionRepository bookEditionRepository;
    private final BookRepository bookRepository;
    // Remove: private final ModelMapper modelMapper; ← DELETE THIS LINE
    private final BookEditionIdGenerator editionIdGenerator;

    @Override
    public BookEditionResponseDTO addEdition(BookEditionRequestDTO request) {
        // Validate book exists
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.getBookId()));

        // Check if ISBN already exists
        bookEditionRepository.findByIsbn(request.getIsbn())
                .ifPresent(edition -> {
                    throw new RuntimeException("ISBN already exists: " + request.getIsbn());
                });

        // Create new edition - MANUAL MAPPING (no ModelMapper)
        BookEdition edition = new BookEdition();
        String lastEditionId = bookEditionRepository.findLastEditionId();
        edition.setEditionId(editionIdGenerator.generateNextId(lastEditionId));
        edition.setBook(book);
        edition.setIsbn(request.getIsbn());
        edition.setPublicationYear(request.getPublicationYear());
        edition.setPublisher(request.getPublisher());
        edition.setLanguage(request.getLanguage());
        edition.setTotalCopies(request.getTotalCopies());
        edition.setAvailableCopies(request.getTotalCopies()); // Initially all copies available
        edition.setPrice(request.getPrice());

        // Save
        BookEdition savedEdition = bookEditionRepository.save(edition);

        // Return response
        return mapToResponseDTO(savedEdition);
    }

    @Override
    public BookEditionResponseDTO getEditionById(String editionId) {
        BookEdition edition = bookEditionRepository.findById(editionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookEdition", editionId));
        return mapToResponseDTO(edition);
    }

    @Override
    public List<BookEditionResponseDTO> getEditionsByBookId(String bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", bookId);
        }
        return bookEditionRepository.findByBook_BookId(bookId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookEditionResponseDTO updateEdition(String editionId, BookEditionRequestDTO request) {
        // Find existing edition
        BookEdition existingEdition = bookEditionRepository.findById(editionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookEdition", editionId));

        // Validate book exists
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", request.getBookId()));

        // Check if ISBN is taken by another edition
        bookEditionRepository.findByIsbn(request.getIsbn())
                .ifPresent(edition -> {
                    if (!edition.getEditionId().equals(editionId)) {
                        throw new RuntimeException("ISBN already exists: " + request.getIsbn());
                    }
                });

        // Update fields - MANUAL SET
        existingEdition.setBook(book);
        existingEdition.setIsbn(request.getIsbn());
        existingEdition.setPublicationYear(request.getPublicationYear());
        existingEdition.setPublisher(request.getPublisher());
        existingEdition.setLanguage(request.getLanguage());
        existingEdition.setTotalCopies(request.getTotalCopies());
        existingEdition.setPrice(request.getPrice());

        // Adjust available copies if total copies changed
        if (existingEdition.getAvailableCopies() > existingEdition.getTotalCopies()) {
            existingEdition.setAvailableCopies(existingEdition.getTotalCopies());
        }

        // Save
        BookEdition updatedEdition = bookEditionRepository.save(existingEdition);
        return mapToResponseDTO(updatedEdition);
    }

    @Override
    public void deleteEdition(String editionId) {
        if (!bookEditionRepository.existsById(editionId)) {
            throw new ResourceNotFoundException("BookEdition", editionId);
        }
        bookEditionRepository.deleteById(editionId);
    }

    @Override
    public BookEditionResponseDTO updateAvailableCopies(String editionId, int newAvailableCopies) {
        BookEdition edition = bookEditionRepository.findById(editionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookEdition", editionId));

        if (newAvailableCopies < 0) {
            throw new RuntimeException("Available copies cannot be negative");
        }
        if (newAvailableCopies > edition.getTotalCopies()) {
            throw new RuntimeException("Available copies cannot exceed total copies: " + edition.getTotalCopies());
        }

        edition.setAvailableCopies(newAvailableCopies);
        BookEdition updatedEdition = bookEditionRepository.save(edition);
        return mapToResponseDTO(updatedEdition);
    }

    @Override
    public List<BookEditionResponseDTO> getEditionsWithAvailableCopies() {
        return bookEditionRepository.findByAvailableCopiesGreaterThan(0)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookEditionResponseDTO> getEditionsByIsbn(String isbn) {
        return bookEditionRepository.findByIsbnContaining(isbn)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // MANUAL MAPPING - No ModelMapper!
    private BookEditionResponseDTO mapToResponseDTO(BookEdition edition) {
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