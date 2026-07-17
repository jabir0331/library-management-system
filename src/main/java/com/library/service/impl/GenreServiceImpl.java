package com.library.service.impl;

import com.library.dto.request.GenreRequestDTO;
import com.library.dto.response.GenreResponseDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Genre;
import com.library.repository.GenreRepository;
import com.library.service.GenreService;
import com.library.util.GenreIdGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
            throw new RuntimeException("Genre already exists: " + request.getName());
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
    public List<GenreResponseDTO> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(genre -> modelMapper.map(genre, GenreResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public GenreResponseDTO updateGenre(String genreId, GenreRequestDTO request) {
        Genre existingGenre = genreRepository.findById(genreId)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", genreId));

        if (!existingGenre.getName().equals(request.getName())
                && genreRepository.existsByName(request.getName())) {
            throw new RuntimeException("Genre already exists: " + request.getName());
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
    public List<GenreResponseDTO> searchGenres(String keyword) {
        return genreRepository.searchGenres(keyword)
                .stream()
                .map(genre -> modelMapper.map(genre, GenreResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByName(String name) {
        return genreRepository.existsByName(name);
    }
}