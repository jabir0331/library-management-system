package com.library.dto.response;

import com.library.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookEditionResponseDTO implements BaseDTO {

    private String editionId;
    private String bookId;
    private String isbn;
    private Integer publicationYear;
    private String publisher;
    private String language;
    private Integer totalCopies;
    private Integer availableCopies;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}