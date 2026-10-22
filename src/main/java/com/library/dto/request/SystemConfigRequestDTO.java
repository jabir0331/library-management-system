package com.library.dto.request;

import com.library.dto.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigRequestDTO implements BaseDTO {

    @NotBlank(message = "Config key is required")
    @Size(min = 2, max = 50, message = "Config key must be between 2 and 50 characters")
    private String configKey;

    @NotBlank(message = "Config value is required")
    private String configValue;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private String dataType = "STRING";
    private boolean isEditable = true;
}