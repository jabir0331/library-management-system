package com.library.dto.seed;

import com.library.model.enums.ConfigDataType;
import lombok.Data;

@Data
public class SystemConfigSeedDTO {
    private String configKey;
    private String configValue;
    private String description;
    private ConfigDataType dataType;
    private boolean isEditable;
}