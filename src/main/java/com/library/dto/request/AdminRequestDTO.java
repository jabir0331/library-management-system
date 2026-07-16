package com.library.dto.request;

import com.library.dto.BaseDTO;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRequestDTO implements BaseDTO {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 25, message = "Username must be between 3 and 25 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must not exceed 50 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 15, message = "Password must be between 6 and 15 characters")
    private String password;
}