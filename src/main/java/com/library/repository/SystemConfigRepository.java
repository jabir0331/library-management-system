package com.library.repository;

import com.library.model.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, String> {

    Optional<SystemConfig> findByConfigKey(String configKey);

    boolean existsByConfigKey(String configKey);

    @Query("SELECT sc.configId FROM SystemConfig sc ORDER BY sc.configId DESC LIMIT 1")
    String findLastConfigId();

    void deleteByConfigKey(String configKey);
}