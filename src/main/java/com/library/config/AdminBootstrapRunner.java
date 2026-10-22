package com.library.config;

import com.library.model.Admin;
import com.library.repository.AdminRepository;
import com.library.util.AdminIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin on an empty database so someone can log in.
 * Only runs when the admin table is empty and BOOTSTRAP_ADMIN_PASSWORD is set.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminIdGenerator adminIdGenerator;

    @Value("${app.bootstrap-admin.username:admin}")
    private String username;
    @Value("${app.bootstrap-admin.email:admin@library.local}")
    private String email;
    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Override
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            return;
        }
        if (password.isBlank()) {
            log.warn("No admins exist and BOOTSTRAP_ADMIN_PASSWORD is not set - nobody will be able to log in.");
            return;
        }
        Admin admin = new Admin();
        admin.setAdminId(adminIdGenerator.generateNextId(adminRepository.findLastAdminId()));
        admin.setUsername(username);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        adminRepository.save(admin);
        log.info("Bootstrap admin '{}' created", username);
    }
}
