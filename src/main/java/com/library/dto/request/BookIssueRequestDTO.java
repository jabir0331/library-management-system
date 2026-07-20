package com.library.dto.request;

import com.library.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookIssueRequestDTO implements BaseDTO {

    @NotBlank(message = "Member ID is required")
    private String memberId;

    @NotBlank(message = "Book Edition ID is required")
    private String bookEditionId;

    @NotBlank(message = "Issued by Admin ID is required")
    private String issuedById;

    @NotNull(message = "Issued date is required")
    private LocalDate issuedDate;

    // Optional due date - if not provided, calculated from config
    private LocalDate dueDate;
}