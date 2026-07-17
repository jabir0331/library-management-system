package com.library.repository;

import com.library.model.BookEdition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookEditionRepository extends JpaRepository<BookEdition, String> {

    @Query("SELECT be.editionId FROM BookEdition be ORDER BY be.editionId DESC LIMIT 1")
    String findLastEditionId();

    List<BookEdition> findByBook_BookId(String bookId);

    // Find by ISBN (exact match)
    Optional<BookEdition> findByIsbn(String isbn);

    // Find by ISBN (partial match)
    List<BookEdition> findByIsbnContaining(String isbn);

    // Find editions with available copies
    List<BookEdition> findByAvailableCopiesGreaterThan(int minCopies);

    @Query("SELECT SUM(be.totalCopies) FROM BookEdition be WHERE be.book.bookId = :bookId")
    Integer sumTotalCopiesByBookId(@Param("bookId") String bookId);

    @Query("SELECT SUM(be.availableCopies) FROM BookEdition be WHERE be.book.bookId = :bookId")
    Integer sumAvailableCopiesByBookId(@Param("bookId") String bookId);
}