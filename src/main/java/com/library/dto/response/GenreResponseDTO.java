package com.library.dto.response;

import com.library.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreResponseDTO implements BaseDTO {
    private String genreId;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}