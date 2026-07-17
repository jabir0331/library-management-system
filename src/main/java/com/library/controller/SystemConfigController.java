package com.library.controller;

import com.library.dto.request.SystemConfigRequestDTO;
import com.library.dto.response.SystemConfigResponseDTO;
import com.library.service.SystemConfigService;  // ← Use interface
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService configService;

    // ═══════════════════════════════════════════════
    // ADMIN ENDPOINTS (Edit Values Only)
    // ═══════════════════════════════════════════════

    @GetMapping
    public ResponseEntity<List<SystemConfigResponseDTO>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAllConfigs());
    }

    @GetMapping("/{configKey}")
    public ResponseEntity<SystemConfigResponseDTO> getConfigByKey(@PathVariable String configKey) {
        return ResponseEntity.ok(configService.getConfigByKey(configKey));
    }

    @PutMapping("/{configKey}")
    public ResponseEntity<SystemConfigResponseDTO> updateConfigValue(
            @PathVariable String configKey,
            @RequestBody String newValue) {
        return ResponseEntity.ok(configService.updateConfigValue(configKey, newValue));
    }

    @GetMapping("/keys")
    public ResponseEntity<List<String>> getAllConfigKeys() {
        return ResponseEntity.ok(configService.getAllConfigs()
                .stream()
                .map(SystemConfigResponseDTO::getConfigKey)
                .collect(java.util.stream.Collectors.toList()));
    }

    // ═══════════════════════════════════════════════
    // SUPER ADMIN / DEVELOPMENT ENDPOINTS
    // (Full CRUD access)
    // ═══════════════════════════════════════════════

    @PostMapping("/seed")
    public ResponseEntity<String> seedConfigs() {
        configService.seedDefaultConfigs();
        return ResponseEntity.ok("Configurations seeded successfully");
    }

    @PostMapping
    public ResponseEntity<SystemConfigResponseDTO> createConfig(
            @Valid @RequestBody SystemConfigRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(configService.createConfig(request));
    }

    @DeleteMapping("/{configKey}")
    public ResponseEntity<Void> deleteConfigByKey(@PathVariable String configKey) {
        configService.deleteConfigByKey(configKey);
        return ResponseEntity.noContent().build();
    }
}