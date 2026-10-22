package com.library.repository;

import com.library.model.Book;
import com.library.model.BookEdition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {

    @Query("SELECT b.bookId FROM Book b ORDER BY b.bookId DESC LIMIT 1")
    String findLastBookId();

    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);

    Page<Book> findByAuthor_AuthorId(String authorId, Pageable pageable);
    Page<Book> findByGenre_GenreId(String genreId, Pageable pageable);
    Page<Book> findByIsAvailableTrue(Pageable pageable);

    // Get editions of a book
    @Query("SELECT be FROM BookEdition be WHERE be.book.bookId = :bookId")
    List<BookEdition> findEditionsByBookId(@Param("bookId") String bookId);
}