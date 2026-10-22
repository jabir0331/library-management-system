package com.library.repository;

import com.library.model.BookEdition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookEditionRepository extends JpaRepository<BookEdition, String> {

    @Query("SELECT be.editionId FROM BookEdition be ORDER BY be.editionId DESC LIMIT 1")
    String findLastEditionId();

    Page<BookEdition> findByBook_BookId(String bookId, Pageable pageable);

    // Find by ISBN (exact match)
    Optional<BookEdition> findByIsbn(String isbn);

    // Find by ISBN (partial match)
    Page<BookEdition> findByIsbnContaining(String isbn, Pageable pageable);

    // Find editions with available copies
    Page<BookEdition> findByAvailableCopiesGreaterThan(int minCopies, Pageable pageable);

    @Query("SELECT SUM(be.totalCopies) FROM BookEdition be WHERE be.book.bookId = :bookId")
    Integer sumTotalCopiesByBookId(@Param("bookId") String bookId);

    @Query("SELECT SUM(be.availableCopies) FROM BookEdition be WHERE be.book.bookId = :bookId")
    Integer sumAvailableCopiesByBookId(@Param("bookId") String bookId);
}