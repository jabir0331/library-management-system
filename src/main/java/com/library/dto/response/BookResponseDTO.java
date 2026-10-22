package com.library.dto.response;

import com.library.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookResponseDTO implements BaseDTO {

    private String bookId;
    private String authorId;
    private String authorName;      // We'll set this manually
    private String genreId;
    private String genreName;       // We'll set this manually
    private String title;
    private boolean isAvailable;
    private String shelfLocation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<BookEditionResponseDTO> editions;
}

