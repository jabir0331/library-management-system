package com.library.service.impl;

import com.library.dto.request.SystemConfigRequestDTO;
import com.library.dto.response.SystemConfigResponseDTO;
import com.library.dto.seed.SystemConfigSeedDTO;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.SystemConfig;
import com.library.model.enums.ConfigDataType;
import com.library.repository.SystemConfigRepository;
import com.library.service.SystemConfigService;
import com.library.util.ConfigIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository configRepository;
    private final ConfigIdGenerator configIdGenerator;
    private final ResourceLoader resourceLoader;

    private static final String SEED_FILE_PATH = "classpath:config/seed-data/system-config.txt";

    // ═══════════════════════════════════════════════
    // AUTO-SEED ON APPLICATION START
    // ═══════════════════════════════════════════════

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void seedDefaultConfigs() {
        log.info("Checking if system configs need to be seeded...");

        if (configRepository.count() > 0) {
            log.info("System configs already exist ({}). Skipping seed.", configRepository.count());
            return;
        }

        log.info("Seeding default system configurations from properties file...");

        try {
            // Load the properties file from resources
            Resource resource = resourceLoader.getResource(SEED_FILE_PATH);

            if (!resource.exists()) {
                log.warn("Seed file not found at: {}. Using hardcoded defaults.", SEED_FILE_PATH);
                seedHardcodedConfigs();
                return;
            }

            // Parse the file line by line
            List<SystemConfigSeedDTO> seedDTOs = parsePropertiesFile(resource);

            if (seedDTOs.isEmpty()) {
                log.warn("No valid configurations found in seed file. Using hardcoded defaults.");
                seedHardcodedConfigs();
                return;
            }

            // Save all configs
            for (SystemConfigSeedDTO dto : seedDTOs) {
                saveConfig(dto);
            }

            log.info("Successfully seeded {} configurations from properties file.", seedDTOs.size());

        } catch (Exception e) {
            log.error("Failed to seed default configurations from file: {}", e.getMessage());
            log.info("Falling back to hardcoded defaults...");
            seedHardcodedConfigs();
        }
    }

    /**
     * Parse the properties file line by line
     * Format: KEY=VALUE|DESCRIPTION|DATATYPE|EDITABLE
     */
    private List<SystemConfigSeedDTO> parsePropertiesFile(Resource resource) throws Exception {
        List<SystemConfigSeedDTO> configs = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    // Parse: KEY=VALUE|DESCRIPTION|DATATYPE|EDITABLE
                    String[] parts = line.split("\\|", -1);

                    if (parts.length < 4) {
                        log.warn("Line {}: Invalid format (expected 4 parts, got {}). Skipping: {}",
                                lineNumber, parts.length, line);
                        continue;
                    }

                    // Parse KEY=VALUE
                    String[] keyValue = parts[0].split("=", 2);
                    if (keyValue.length < 2) {
                        log.warn("Line {}: Invalid key=value format. Skipping: {}", lineNumber, line);
                        continue;
                    }

                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    String description = parts[1].trim();
                    String dataTypeStr = parts[2].trim().toUpperCase();
                    boolean editable = Boolean.parseBoolean(parts[3].trim());

                    // Validate data type
                    ConfigDataType dataType;
                    try {
                        dataType = ConfigDataType.valueOf(dataTypeStr);
                    } catch (IllegalArgumentException e) {
                        log.warn("Line {}: Invalid data type '{}'. Using STRING. Line: {}",
                                lineNumber, dataTypeStr, line);
                        dataType = ConfigDataType.STRING;
                    }

                    // Validate value based on data type
                    if (!isValidValue(value, dataType)) {
                        log.warn("Line {}: Invalid value '{}' for data type {}. Skipping.",
                                lineNumber, value, dataType);
                        continue;
                    }

                    SystemConfigSeedDTO dto = new SystemConfigSeedDTO();
                    dto.setConfigKey(key);
                    dto.setConfigValue(value);
                    dto.setDescription(description);
                    dto.setDataType(dataType);
                    dto.setEditable(editable);

                    configs.add(dto);
                    log.debug("Parsed config: {} = {} ({})", key, value, dataType);

                } catch (Exception e) {
                    log.warn("Line {}: Error parsing line: {}. Error: {}", lineNumber, line, e.getMessage());
                }
            }
        }

        return configs;
    }

    /**
     * Validate value based on data type
     */
    private boolean isValidValue(String value, ConfigDataType dataType) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        try {
            switch (dataType) {
                case INTEGER:
                    Integer.parseInt(value);
                    return true;
                case DECIMAL:
                    new BigDecimal(value);
                    return true;
                case BOOLEAN:
                    Boolean.parseBoolean(value);
                    return true;
                case DATE:
                    java.time.LocalDate.parse(value);
                    return true;
                case STRING:
                    return true;
                default:
                    return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Fallback: Seed hardcoded configurations if file is not available
     */
    private void seedHardcodedConfigs() {
        log.info("Using hardcoded default configurations...");

        List<SystemConfigSeedDTO> defaults = getDefaultConfigs();

        for (SystemConfigSeedDTO dto : defaults) {
            saveConfig(dto);
        }

        log.info("Successfully seeded {} configurations from hardcoded defaults.", defaults.size());
    }

    /**
     * Get default configurations
     */
    private List<SystemConfigSeedDTO> getDefaultConfigs() {
        List<SystemConfigSeedDTO> configs = new ArrayList<>();

        configs.add(createDTO("MAX_ISSUE_DAYS", "14", "Maximum days a book can be issued", ConfigDataType.INTEGER, true));
        configs.add(createDTO("FINE_PER_DAY", "25.00", "Fine amount per day overdue (in LKR)", ConfigDataType.DECIMAL, true));
        configs.add(createDTO("MAX_BOOKS_PER_MEMBER", "3", "Maximum books a member can borrow", ConfigDataType.INTEGER, true));
        configs.add(createDTO("LOST_BOOK_MULTIPLIER", "1.5", "Multiplier for lost book penalty (price × multiplier)", ConfigDataType.DECIMAL, true));
        configs.add(createDTO("SERVICE_CHARGE", "250.00", "Service charge for lost books (in LKR)", ConfigDataType.DECIMAL, true));
        configs.add(createDTO("GRACE_PERIOD_DAYS", "0", "Grace period before fine applies", ConfigDataType.INTEGER, true));
        configs.add(createDTO("MAX_RENEWAL_COUNT", "1", "Maximum times a book can be renewed", ConfigDataType.INTEGER, true));
        configs.add(createDTO("RENEWAL_DAYS", "7", "Additional days when renewing", ConfigDataType.INTEGER, true));
        configs.add(createDTO("MEMBERSHIP_FEE", "0.00", "Annual membership fee (in LKR)", ConfigDataType.DECIMAL, true));

        return configs;
    }

    /**
     * Helper to create DTO
     */
    private SystemConfigSeedDTO createDTO(String key, String value, String description,
                                          ConfigDataType dataType, boolean editable) {
        SystemConfigSeedDTO dto = new SystemConfigSeedDTO();
        dto.setConfigKey(key);
        dto.setConfigValue(value);
        dto.setDescription(description);
        dto.setDataType(dataType);
        dto.setEditable(editable);
        return dto;
    }

    /**
     * Save a single configuration
     */
    private void saveConfig(SystemConfigSeedDTO dto) {
        SystemConfig config = new SystemConfig();
        String lastId = configRepository.findLastConfigId();
        config.setConfigId(configIdGenerator.generateNextId(lastId));
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setDescription(dto.getDescription());
        config.setDataType(dto.getDataType());
        config.setEditable(dto.isEditable());

        configRepository.save(config);
        log.info("Seeded config: {} = {} ({})", dto.getConfigKey(), dto.getConfigValue(), dto.getDataType());
    }

    // ═══════════════════════════════════════════════
    // CRUD OPERATIONS
    // ═══════════════════════════════════════════════

    @Override
    public SystemConfigResponseDTO createConfig(SystemConfigRequestDTO request) {
        if (configRepository.existsByConfigKey(request.getConfigKey())) {
            throw new BusinessException("Config key already exists: " + request.getConfigKey());
        }

        SystemConfig config = new SystemConfig();
        String lastId = configRepository.findLastConfigId();
        config.setConfigId(configIdGenerator.generateNextId(lastId));
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setDescription(request.getDescription());

        try {
            config.setDataType(ConfigDataType.valueOf(request.getDataType().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid data type: " + request.getDataType());
        }

        config.setEditable(request.isEditable());

        SystemConfig savedConfig = configRepository.save(config);
        return mapToResponseDTO(savedConfig);
    }

    @Override
    public SystemConfigResponseDTO getConfigById(String configId) {
        SystemConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("SystemConfig", configId));
        return mapToResponseDTO(config);
    }

    @Override
    public SystemConfigResponseDTO getConfigByKey(String configKey) {
        SystemConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with key: " + configKey));
        return mapToResponseDTO(config);
    }

    @Override
    public List<SystemConfigResponseDTO> getAllConfigs() {
        return configRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemConfigResponseDTO updateConfigValue(String configKey, String newValue) {
        SystemConfig config = configRepository.findByConfigKey(configKey)
                .orElseThrow(() -> new ResourceNotFoundException("Config not found with key: " + configKey));

        if (!config.isEditable()) {
            throw new BusinessException("Config is not editable: " + configKey);
        }

        // Validate value based on data type
        if (!isValidValue(newValue, config.getDataType())) {
            throw new ValidationException("Invalid value '" + newValue + "' for data type: " + config.getDataType());
        }

        config.setConfigValue(newValue);
        SystemConfig updatedConfig = configRepository.save(config);
        return mapToResponseDTO(updatedConfig);
    }

    @Override
    public void deleteConfig(String configId) {
        if (!configRepository.existsById(configId)) {
            throw new ResourceNotFoundException("SystemConfig", configId);
        }
        configRepository.deleteById(configId);
    }

    @Override
    public void deleteConfigByKey(String configKey) {
        if (!configRepository.existsByConfigKey(configKey)) {
            throw new ResourceNotFoundException("Config not found with key: " + configKey);
        }
        configRepository.deleteByConfigKey(configKey);
    }

    // ═══════════════════════════════════════════════
    // TYPE-SAFE GETTERS FOR BUSINESS LOGIC
    // ═══════════════════════════════════════════════

    @Override
    public int getMaxIssueDays() {
        return getIntConfig("MAX_ISSUE_DAYS");
    }

    @Override
    public BigDecimal getFinePerDay() {
        return getDecimalConfig("FINE_PER_DAY");
    }

    @Override
    public int getMaxBooksPerMember() {
        return getIntConfig("MAX_BOOKS_PER_MEMBER");
    }

    @Override
    public BigDecimal getLostBookMultiplier() {
        return getDecimalConfig("LOST_BOOK_MULTIPLIER");
    }

    @Override
    public BigDecimal getServiceCharge() {
        return getDecimalConfig("SERVICE_CHARGE");
    }

    @Override
    public int getGracePeriodDays() {
        return getIntConfig("GRACE_PERIOD_DAYS");
    }

    @Override
    public int getMaxRenewalCount() {
        return getIntConfig("MAX_RENEWAL_COUNT");
    }

    @Override
    public int getRenewalDays() {
        return getIntConfig("RENEWAL_DAYS");
    }

    @Override
    public BigDecimal getMembershipFee() {
        return getDecimalConfig("MEMBERSHIP_FEE");
    }

    // ═══════════════════════════════════════════════
    // PRIVATE HELPER METHODS
    // ═══════════════════════════════════════════════

    private int getIntConfig(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getIntValue)
                .orElse(0);
    }

    private BigDecimal getDecimalConfig(String key) {
        return configRepository.findByConfigKey(key)
                .map(SystemConfig::getDecimalValue)
                .orElse(BigDecimal.ZERO);
    }

    private SystemConfigResponseDTO mapToResponseDTO(SystemConfig config) {
        SystemConfigResponseDTO response = new SystemConfigResponseDTO();
        response.setConfigId(config.getConfigId());
        response.setConfigKey(config.getConfigKey());
        response.setConfigValue(config.getConfigValue());
        response.setDescription(config.getDescription());
        response.setDataType(config.getDataType().name());
        response.setEditable(config.isEditable());
        response.setCreatedAt(config.getCreatedAt());
        response.setUpdatedAt(config.getUpdatedAt());
        return response;
    }
}