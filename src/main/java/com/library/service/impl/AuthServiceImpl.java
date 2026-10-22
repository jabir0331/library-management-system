package com.library.service.impl;

import com.library.dto.request.LoginRequestDTO;
import com.library.dto.response.AuthResponseDTO;
import com.library.model.Admin;
import com.library.model.RevokedToken;
import com.library.repository.AdminRepository;
import com.library.repository.RevokedTokenRepository;
import com.library.security.JwtService;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    public void logout(String token) {
        jwtService.parse(token).ifPresent(claims -> {
            revokedTokenRepository.save(new RevokedToken(claims.getId(), claims.getExpiration().toInstant()));
            // Expired tokens are rejected anyway, so their rows can go
            revokedTokenRepository.deleteExpired(Instant.now());
        });
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {
        Admin admin = adminRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        String stored = admin.getPassword();
        boolean hashed = stored.startsWith("$2");

        // Legacy rows created before BCrypt was introduced hold plain text: verify, then upgrade to a hash.
        boolean matches = hashed
                ? passwordEncoder.matches(request.getPassword(), stored)
                : stored.equals(request.getPassword());
        if (!matches) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!hashed) {
            admin.setPassword(passwordEncoder.encode(request.getPassword()));
            adminRepository.save(admin);
            log.info("Upgraded legacy plain-text password to BCrypt for admin {}", admin.getAdminId());
        }

        String token = jwtService.generateToken(admin.getUsername(), admin.getAdminId());
        return new AuthResponseDTO(token, "Bearer", jwtService.getExpirationMs(),
                admin.getAdminId(), admin.getUsername());
    }
}
