package com.library.repository;

import com.library.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, String> {

    Optional<Genre> findByName(String name);
    boolean existsByName(String name);

    @Query("SELECT g.genreId FROM Genre g ORDER BY g.genreId DESC LIMIT 1")
    String findLastGenreId();

    @Query("SELECT g FROM Genre g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Genre> searchGenres(String keyword);
}