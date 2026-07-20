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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<AuthorResponseDTO> getAllAuthors() {
        return authorRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthorResponseDTO> getActiveAuthors() {
        return authorRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AuthorResponseDTO> getInactiveAuthors() {
        return authorRepository.findByIsActiveFalse()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
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
    public List<AuthorResponseDTO> searchAuthors(String keyword) {
        return authorRepository.searchAuthors(keyword)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
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