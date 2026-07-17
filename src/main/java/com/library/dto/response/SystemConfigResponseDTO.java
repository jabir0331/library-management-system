package com.library.dto.response;

import com.library.dto.BaseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigResponseDTO implements BaseDTO {

    private String configId;
    private String configKey;
    private String configValue;
    private String description;
    private String dataType;
    private boolean isEditable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}