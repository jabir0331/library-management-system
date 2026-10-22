package com.library.service.impl;

import com.library.dto.request.AuthorRequestDTO;
import com.library.dto.response.AuthorResponseDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Author;
import com.library.repository.AuthorRepository;
import com.library.service.AuthorService;
import com.library.util.AuthorIdGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final ModelMapper modelMapper;
    private final AuthorIdGenerator idGenerator;

    @Override
    public AuthorResponseDTO createAuthor(AuthorRequestDTO request) {
        // Validate email uniqueness
        if (authorRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered: " + request.getEmail());
        }

        // Convert DTO to Entity
        Author author = modelMapper.map(request, Author.class);

        // Generate custom author ID
        String lastId = authorRepository.findLastAuthorId();
        String newId = idGenerator.generateNextId(lastId);
        author.setAuthorId(newId);

        // Set default active status
        author.setActive(true);

        // Save to database
        Author savedAuthor = authorRepository.save(author);

        // Convert to Response DTO
        return mapToResponseDTO(savedAuthor);
    }

    @Override
    public AuthorResponseDTO getAuthorById(String authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));
        return mapToResponseDTO(author);
    }

    @Override
    public AuthorResponseDTO getAuthorByEmail(String email) {
        Author author = authorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "email", email));
        return mapToResponseDTO(author);
    }

    @Override
    public Page<AuthorResponseDTO> getAllAuthors(Pageable pageable) {
        return authorRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Page<AuthorResponseDTO> getActiveAuthors(Pageable pageable) {
        return authorRepository.findByIsActiveTrue(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public Page<AuthorResponseDTO> getInactiveAuthors(Pageable pageable) {
        return authorRepository.findByIsActiveFalse(pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public AuthorResponseDTO updateAuthor(String authorId, AuthorRequestDTO request) {
        // Find existing author
        Author existingAuthor = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));

        // Check if email changed and if new email is taken
        if (!existingAuthor.getEmail().equals(request.getEmail())
                && authorRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered: " + request.getEmail());
        }

        // Update fields
        existingAuthor.setFirstName(request.getFirstName());
        existingAuthor.setLastName(request.getLastName());
        existingAuthor.setEmail(request.getEmail());
        existingAuthor.setNationality(request.getNationality());

        // Save updated author
        Author updatedAuthor = authorRepository.save(existingAuthor);

        // Convert to Response DTO
        return mapToResponseDTO(updatedAuthor);
    }

    @Override
    public void deleteAuthor(String authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author", authorId);
        }
        authorRepository.deleteById(authorId);
    }

    @Override
    public AuthorResponseDTO activateAuthor(String authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));

        author.setActive(true);
        Author updatedAuthor = authorRepository.save(author);

        return mapToResponseDTO(updatedAuthor);
    }

    @Override
    public AuthorResponseDTO deactivateAuthor(String authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", authorId));

        author.setActive(false);
        Author updatedAuthor = authorRepository.save(author);

        return mapToResponseDTO(updatedAuthor);
    }

    @Override
    public Page<AuthorResponseDTO> searchAuthors(String keyword, Pageable pageable) {
        return authorRepository.searchAuthors(keyword, pageable)
                .map(this::mapToResponseDTO);
    }

    @Override
    public boolean existsByEmail(String email) {
        return authorRepository.existsByEmail(email);
    }

    // Helper method to map Entity to Response DTO
    private AuthorResponseDTO mapToResponseDTO(Author author) {
        AuthorResponseDTO response = modelMapper.map(author, AuthorResponseDTO.class);
        response.setFullName(author.getFullName());
        return response;
    }
}