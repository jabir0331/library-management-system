package com.library.dto.request;

import com.library.dto.BaseDTO;
import com.library.model.enums.ReturnCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookReturnRequestDTO implements BaseDTO {

    @NotBlank(message = "Issue ID is required")
    private String issueId;

    @NotNull(message = "Returned date is required")
    private LocalDate returnedDate;

    @NotBlank(message = "Received by Admin ID is required")
    private String receivedById;

    private ReturnCondition returnCondition = ReturnCondition.GOOD;
}