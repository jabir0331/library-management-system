package com.library.controller;

import com.library.dto.request.SystemConfigRequestDTO;
import com.library.dto.response.SystemConfigResponseDTO;
import com.library.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@Tag(name = "System Configuration", description = "Endpoints for managing system configurations and business rules")
public class SystemConfigController {

    private final SystemConfigService configService;

    // ═══════════════════════════════════════════════
    // ADMIN ENDPOINTS (Edit Values Only)
    // ═══════════════════════════════════════════════

    @GetMapping
    @Operation(
            summary = "Get All System Configurations",
            description = "Retrieves a list of all system configurations with their keys, values, and metadata"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configurations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SystemConfigResponseDTO.class))
            )
    })
    public ResponseEntity<List<SystemConfigResponseDTO>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @GetMapping("/{configKey}")
    @Operation(
            summary = "Get System Configuration by Key",
            description = "Retrieves a specific system configuration by its key (e.g., MAX_ISSUE_DAYS)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration found",
                    content = @Content(schema = @Schema(implementation = SystemConfigResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Config not found with key: NON_EXISTENT_KEY\"}")
                    )
            )
    })
    public ResponseEntity<SystemConfigResponseDTO> getConfigByKey(
            @Parameter(description = "Configuration key (e.g., MAX_ISSUE_DAYS)", required = true, example = "MAX_ISSUE_DAYS")
            @PathVariable String configKey) {
        return ResponseEntity.ok(configService.getConfigByKey(configKey));
    }

    @PutMapping("/{configKey}")
    @Operation(
            summary = "Update Configuration Value",
            description = "Updates the value of an existing system configuration. " +
                    "Value is validated against the configured data type (INTEGER, DECIMAL, BOOLEAN, etc.)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = SystemConfigResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid value for data type or config is not editable",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Invalid Type",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Invalid value 'invalid' for data type: INTEGER\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Not Editable",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Config is not editable: MAX_ISSUE_DAYS\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Config not found with key: NON_EXISTENT_KEY\"}")
                    )
            )
    })
    public ResponseEntity<SystemConfigResponseDTO> updateConfigValue(
            @Parameter(description = "Configuration key (e.g., MAX_ISSUE_DAYS)", required = true, example = "MAX_ISSUE_DAYS")
            @PathVariable String configKey,
            @Parameter(description = "New value (must match the configured data type)", required = true, example = "21")
            @RequestBody String newValue) {
        return ResponseEntity.ok(configService.updateConfigValue(configKey, newValue));
    }

    @GetMapping("/keys")
    @Operation(
            summary = "Get All Configuration Keys",
            description = "Retrieves a list of all configuration keys"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Keys retrieved successfully",
                    content = @Content(schema = @Schema(implementation = String.class))
            )
    })
    public ResponseEntity<List<String>> getAllConfigKeys() {
        return ResponseEntity.ok(configService.getAllConfigs()
                .stream()
                .map(SystemConfigResponseDTO::getConfigKey)
                .collect(Collectors.toList()));
    }

    // ═══════════════════════════════════════════════
    // SUPER ADMIN / DEVELOPMENT ENDPOINTS
    // (Full CRUD access)
    // ═══════════════════════════════════════════════

    @PostMapping("/seed")
    @Operation(
            summary = "Seed Default Configurations",
            description = "Seeds the database with default system configurations from the properties file. " +
                    "Only works if no configurations exist (first run)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Configurations seeded successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "Configurations seeded successfully")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Configurations already exist",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"System configs already exist\"}")
                    )
            )
    })
    public ResponseEntity<String> seedConfigs() {
        configService.seedDefaultConfigs();
        return ResponseEntity.ok("Configurations seeded successfully");
    }

    @PostMapping
    @Operation(
            summary = "Create New Configuration",
            description = "Creates a new system configuration. " +
                    "Use this to add custom configurations not available in the default seed file. " +
                    "(Requires SUPER_ADMIN role)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Configuration created successfully",
                    content = @Content(schema = @Schema(implementation = SystemConfigResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Configuration key already exists or invalid data type",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "Duplicate Key",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Config key already exists: MAX_ISSUE_DAYS\"}"
                                    ),
                                    @ExampleObject(
                                            name = "Invalid Data Type",
                                            value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Invalid data type: INVALID_TYPE\"}"
                                    )
                            }
                    )
            )
    })
    public ResponseEntity<SystemConfigResponseDTO> createConfig(
            @Parameter(description = "Configuration details to create", required = true)
            @Valid @RequestBody SystemConfigRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configService.createConfig(request));
    }

    @DeleteMapping("/{configKey}")
    @Operation(
            summary = "Delete Configuration",
            description = "Permanently deletes a system configuration by its key. " +
                    "Use with caution - this may affect business logic. " +
                    "(Requires SUPER_ADMIN role)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Configuration deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Configuration not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Config not found with key: NON_EXISTENT_KEY\"}")
                    )
            )
    })
    public ResponseEntity<Void> deleteConfigByKey(
            @Parameter(description = "Configuration key to delete (e.g., TEST_CONFIG)", required = true, example = "TEST_CONFIG")
            @PathVariable String configKey) {
        configService.deleteConfigByKey(configKey);
        return ResponseEntity.noContent().build();
    }
}