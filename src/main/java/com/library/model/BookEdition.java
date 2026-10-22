package com.library.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "book_edition")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookEdition {

    @Id
    @Column(name = "edition_id", length = 10)
    private String editionId;

    @NotNull(message = "Book is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotBlank(message = "ISBN is required")
    @Size(min = 10, max = 20, message = "ISBN must be between 10 and 20 characters")
    @Column(name = "isbn", unique = true, nullable = false, length = 20)
    private String isbn;

    @NotNull(message = "Publication year is required")
    @Min(value = 1000, message = "Publication year must be at least 1000")
    @Max(value = 9999, message = "Publication year must be at most 9999")
    @Column(name = "publication_year", nullable = false)
    private Integer publicationYear;

    @Size(max = 255, message = "Publisher name must not exceed 255 characters")
    @Column(name = "publisher", length = 255)
    private String publisher;

    @Size(max = 50, message = "Language must not exceed 50 characters")
    @Column(name = "language", length = 50)
    private String language;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Column(name = "total_copies", nullable = false)
    private Integer totalCopies;

    @NotNull(message = "Available copies is required")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Column(name = "available_copies", nullable = false)
    private Integer availableCopies;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price cannot be negative")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed 999999.99")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business methods
    public void decrementAvailableCopies() {
        if (this.availableCopies <= 0) {
            throw new IllegalStateException("No copies available for edition: " + editionId);
        }
        this.availableCopies--;
    }

    public void incrementAvailableCopies() {
        if (this.availableCopies >= this.totalCopies) {
            throw new IllegalStateException("Cannot exceed total copies for edition: " + editionId);
        }
        this.availableCopies++;
    }
}