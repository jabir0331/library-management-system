package com.library.model;

import com.library.model.enums.ConfigDataType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {

    @Id
    @Column(name = "config_id", length = 10)
    private String configId;

    @NotBlank(message = "Config key is required")
    @Size(min = 2, max = 50, message = "Config key must be between 2 and 50 characters")
    @Column(name = "config_key", unique = true, nullable = false, length = 50)
    private String configKey;

    @NotBlank(message = "Config value is required")
    @Column(name = "config_value", nullable = false, length = 255)
    private String configValue;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private ConfigDataType dataType = ConfigDataType.STRING;

    @Column(name = "is_editable")
    private boolean isEditable = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Type-safe getters
    public Integer getIntValue() {
        return Integer.parseInt(configValue);
    }

    public BigDecimal getDecimalValue() {
        return new BigDecimal(configValue);
    }

    public Boolean getBooleanValue() {
        return Boolean.parseBoolean(configValue);
    }

    public LocalDate getDateValue() {
        return LocalDate.parse(configValue);
    }
}