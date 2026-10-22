package com.library.dto.request;

import com.library.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDTO implements BaseDTO {

    @NotBlank(message = "Author ID is required")
    private String authorId;

    @NotBlank(message = "Genre ID is required")
    private String genreId;

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    @Size(max = 50, message = "Shelf location must not exceed 50 characters")
    private String shelfLocation;
}