package com.library.repository;

import com.library.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, String> {

    // Find by email
    Optional<Author> findByEmail(String email);

    // Check if email exists
    boolean existsByEmail(String email);

    // Find active authors only
    List<Author> findByIsActiveTrue();

    // Find inactive authors
    List<Author> findByIsActiveFalse();

    // Search by first name, last name, or email
    @Query("SELECT a FROM Author a WHERE " +
            "LOWER(a.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Author> searchAuthors(@Param("keyword") String keyword);

    // Get the last author ID (for generating next ID)
    @Query("SELECT a.authorId FROM Author a ORDER BY a.authorId DESC LIMIT 1")
    String findLastAuthorId();

    // Find by full name (first + last)
    @Query("SELECT a FROM Author a WHERE " +
            "LOWER(a.firstName) = LOWER(:firstName) AND " +
            "LOWER(a.lastName) = LOWER(:lastName)")
    List<Author> findByFirstNameAndLastName(@Param("firstName") String firstName,
                                            @Param("lastName") String lastName);
}