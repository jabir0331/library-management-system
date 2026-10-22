package com.library.service.impl;

import com.library.dto.request.GenreRequestDTO;
import com.library.dto.response.GenreResponseDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Genre;
import com.library.repository.GenreRepository;
import com.library.service.GenreService;
import com.library.util.GenreIdGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;
    private final ModelMapper modelMapper;
    private final GenreIdGenerator idGenerator;

    @Override
    public GenreResponseDTO createGenre(GenreRequestDTO request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new ValidationException("Genre already exists: " + request.getName());
        }

        Genre genre = modelMapper.map(request, Genre.class);
        String lastId = genreRepository.findLastGenreId();
        genre.setGenreId(idGenerator.generateNextId(lastId));

        Genre savedGenre = genreRepository.save(genre);
        return modelMapper.map(savedGenre, GenreResponseDTO.class);
    }

    @Override
    public GenreResponseDTO getGenreById(String genreId) {
        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", genreId));
        return modelMapper.map(genre, GenreResponseDTO.class);
    }

    @Override
    public GenreResponseDTO getGenreByName(String name) {
        Genre genre = genreRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "name", name));
        return modelMapper.map(genre, GenreResponseDTO.class);
    }

    @Override
    public Page<GenreResponseDTO> getAllGenres(Pageable pageable) {
        return genreRepository.findAll(pageable)
                .map(genre -> modelMapper.map(genre, GenreResponseDTO.class));
    }

    @Override
    public GenreResponseDTO updateGenre(String genreId, GenreRequestDTO request) {
        Genre existingGenre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", genreId));

        if (!existingGenre.getName().equals(request.getName())
                && genreRepository.existsByName(request.getName())) {
            throw new ValidationException("Genre already exists: " + request.getName());
        }

        existingGenre.setName(request.getName());
        Genre updatedGenre = genreRepository.save(existingGenre);
        return modelMapper.map(updatedGenre, GenreResponseDTO.class);
    }

    @Override
    public void deleteGenre(String genreId) {
        if (!genreRepository.existsById(genreId)) {
            throw new ResourceNotFoundException("Genre", genreId);
        }
        genreRepository.deleteById(genreId);
    }

    @Override
    public Page<GenreResponseDTO> searchGenres(String keyword, Pageable pageable) {
        return genreRepository.searchGenres(keyword, pageable)
                .map(genre -> modelMapper.map(genre, GenreResponseDTO.class));
    }

    @Override
    public boolean existsByName(String name) {
        return genreRepository.existsByName(name);
    }
}