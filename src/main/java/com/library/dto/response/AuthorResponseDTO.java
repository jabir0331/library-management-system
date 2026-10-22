package com.library.dto.response;

import com.library.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorResponseDTO implements BaseDTO {

    private String authorId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String nationality;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}