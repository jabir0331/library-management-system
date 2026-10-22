package com.library.service;

import com.library.dto.request.LoginRequestDTO;
import com.library.dto.response.AuthResponseDTO;
import com.library.model.Admin;
import com.library.model.RevokedToken;
import com.library.repository.AdminRepository;
import com.library.repository.RevokedTokenRepository;
import com.library.security.JwtService;
import com.library.service.impl.AuthServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthServiceImplTest {

    @Mock AdminRepository adminRepository;
    @Mock RevokedTokenRepository revokedTokenRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtService jwtService = new JwtService("unit-test-secret-unit-test-secret-1234", 3_600_000);

    private AuthServiceImpl service;
    private Admin admin;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(adminRepository, passwordEncoder, jwtService, revokedTokenRepository);

        admin = new Admin();
        admin.setAdminId("ADM000001");
        admin.setUsername("librarian");
        admin.setPassword(passwordEncoder.encode("Secret123"));
        when(adminRepository.findByUsername("librarian")).thenReturn(Optional.of(admin));
    }

    private LoginRequestDTO login(String username, String password) {
        return new LoginRequestDTO(username, password);
    }

    @Test
    void loginWithCorrectPasswordReturnsAUsableToken() {
        AuthResponseDTO response = service.login(login("librarian", "Secret123"));

        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getAdminId()).isEqualTo("ADM000001");
        assertThat(response.getUsername()).isEqualTo("librarian");
        assertThat(response.getExpiresInMs()).isEqualTo(3_600_000);
        assertThat(jwtService.parse(response.getToken())).isPresent();
        verify(adminRepository, never()).save(any());
    }

    @Test
    void wrongPasswordIsRejected() {
        assertThatThrownBy(() -> service.login(login("librarian", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void unknownUserGetsTheSameErrorAsAWrongPassword() {
        assertThatThrownBy(() -> service.login(login("nobody", "Secret123")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void legacyPlainTextPasswordIsUpgradedToBcryptOnFirstLogin() {
        admin.setPassword("Plain123");

        service.login(login("librarian", "Plain123"));

        ArgumentCaptor<Admin> saved = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(saved.capture());
        assertThat(saved.getValue().getPassword()).startsWith("$2");
        assertThat(passwordEncoder.matches("Plain123", saved.getValue().getPassword())).isTrue();
    }

    @Test
    void wrongPasswordOnLegacyAccountDoesNotChangeIt() {
        admin.setPassword("Plain123");

        assertThatThrownBy(() -> service.login(login("librarian", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        assertThat(admin.getPassword()).isEqualTo("Plain123");
        verify(adminRepository, never()).save(any());
    }

    @Test
    void logoutRevokesTheTokenIdUntilItExpires() {
        String token = jwtService.generateToken("librarian", "ADM000001");
        Claims claims = jwtService.parse(token).orElseThrow();

        service.logout(token);

        ArgumentCaptor<RevokedToken> revoked = ArgumentCaptor.forClass(RevokedToken.class);
        verify(revokedTokenRepository).save(revoked.capture());
        assertThat(revoked.getValue().getJti()).isEqualTo(claims.getId());
        assertThat(revoked.getValue().getExpiresAt()).isEqualTo(claims.getExpiration().toInstant());
        verify(revokedTokenRepository).deleteExpired(any(Instant.class));
    }

    @Test
    void logoutWithAnInvalidTokenDoesNothing() {
        service.logout("garbage");

        verify(revokedTokenRepository, never()).save(any());
    }
}
