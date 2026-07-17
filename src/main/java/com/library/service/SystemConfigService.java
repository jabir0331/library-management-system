package com.library.service;

import com.library.dto.request.SystemConfigRequestDTO;
import com.library.dto.response.SystemConfigResponseDTO;

import java.math.BigDecimal;
import java.util.List;

public interface SystemConfigService {

    // Auto-seed
    void seedDefaultConfigs();

    // CRUD Operations
    SystemConfigResponseDTO createConfig(SystemConfigRequestDTO request);
    SystemConfigResponseDTO getConfigById(String configId);
    SystemConfigResponseDTO getConfigByKey(String configKey);
    List<SystemConfigResponseDTO> getAllConfigs();
    SystemConfigResponseDTO updateConfigValue(String configKey, String newValue);
    void deleteConfig(String configId);
    void deleteConfigByKey(String configKey);

    // Type-safe getters for business logic
    int getMaxIssueDays();
    BigDecimal getFinePerDay();
    int getMaxBooksPerMember();
    BigDecimal getLostBookMultiplier();
    BigDecimal getServiceCharge();
    int getGracePeriodDays();
    int getMaxRenewalCount();
    int getRenewalDays();
    BigDecimal getMembershipFee();
}