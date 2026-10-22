package com.library.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    private final JwtService jwtService = new JwtService(SECRET, 3_600_000);

    @Test
    void tokenRoundTripsSubjectAndAdminId() {
        String token = jwtService.generateToken("librarian", "ADM000001");

        Claims claims = jwtService.parse(token).orElseThrow();

        assertThat(claims.getSubject()).isEqualTo("librarian");
        assertThat(claims.get("adminId", String.class)).isEqualTo("ADM000001");
        assertThat(claims.getId()).isNotBlank();
    }

    @Test
    void everyTokenGetsAUniqueId() {
        String first = jwtService.parse(jwtService.generateToken("a", "ADM000001")).orElseThrow().getId();
        String second = jwtService.parse(jwtService.generateToken("a", "ADM000001")).orElseThrow().getId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken("librarian", "ADM000001");

        assertThat(jwtService.parse(token + "x")).isEmpty();
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        JwtService other = new JwtService("a-completely-different-secret-value-9876", 3_600_000);

        assertThat(jwtService.parse(other.generateToken("librarian", "ADM000001"))).isEmpty();
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService shortLived = new JwtService(SECRET, -1_000);

        assertThat(jwtService.parse(shortLived.generateToken("librarian", "ADM000001"))).isEmpty();
    }

    @Test
    void garbageIsRejected() {
        assertThat(jwtService.parse("not.a.jwt")).isEmpty();
        assertThat(jwtService.parse("")).isEmpty();
    }

    @Test
    void shortSecretIsRefusedAtStartup() {
        assertThatThrownBy(() -> new JwtService("too-short", 1000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32");
    }
}
