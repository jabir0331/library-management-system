package com.library.controller;

import com.library.dto.response.AuthResponseDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.model.Admin;
import com.library.repository.AdminRepository;
import com.library.repository.RevokedTokenRepository;
import com.library.security.AdminUserDetailsService;
import com.library.security.JwtAuthenticationFilter;
import com.library.security.JwtService;
import com.library.security.SecurityConfig;
import com.library.service.AuthService;
import com.library.service.BookIssueService;
import com.library.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies endpoint protection, token handling and error mapping through the real security filter chain. */
@WebMvcTest(controllers = {AuthController.class, MemberController.class, BookIssueController.class},
        properties = {
                "app.jwt.secret=web-test-secret-web-test-secret-123456",
                "app.jwt.expiration-ms=3600000"
        })
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, AdminUserDetailsService.class})
class SecurityWebTest {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;

    @MockitoBean AuthService authService;
    @MockitoBean MemberService memberService;
    @MockitoBean BookIssueService bookIssueService;
    @MockitoBean AdminRepository adminRepository;
    @MockitoBean RevokedTokenRepository revokedTokenRepository;

    private String token;

    @BeforeEach
    void setUp() {
        Admin admin = new Admin();
        admin.setAdminId("ADM000007");
        admin.setUsername("librarian");
        admin.setPassword("$2a$10$irrelevant");
        when(adminRepository.findByUsername("librarian")).thenReturn(Optional.of(admin));
        when(revokedTokenRepository.existsById(any())).thenReturn(false);
        token = jwtService.generateToken("librarian", "ADM000007");
    }

    private String bearer(String t) {
        return "Bearer " + t;
    }

    // ── protection ──────────────────────────────────────────

    @Test
    void protectedEndpointWithoutTokenIs401Json() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    void validTokenGrantsAccess() throws Exception {
        when(memberService.getAllMembers()).thenReturn(List.of());

        mockMvc.perform(get("/api/members").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/members").header("Authorization", bearer(token + "x")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonBearerAuthorizationHeaderIsRejected() throws Exception {
        mockMvc.perform(get("/api/members").header("Authorization", "Basic abc"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void revokedTokenIsRejected() throws Exception {
        String jti = jwtService.parse(token).orElseThrow().getId();
        when(revokedTokenRepository.existsById(jti)).thenReturn(true);

        mockMvc.perform(get("/api/members").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenOfADeletedAdminIsRejected() throws Exception {
        when(adminRepository.findByUsername("librarian")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/members").header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    // ── login / logout ──────────────────────────────────────

    @Test
    void loginIsPublicAndReturnsTheToken() throws Exception {
        when(authService.login(any())).thenReturn(
                new AuthResponseDTO("jwt-value", "Bearer", 3600000, "ADM000007", "librarian"));

        mockMvc.perform(post("/api/auth/login").contentType(JSON)
                        .content("{\"username\":\"librarian\",\"password\":\"Secret123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void badCredentialsAre401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login").contentType(JSON)
                        .content("{\"username\":\"librarian\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    @Test
    void blankLoginFieldsAre400() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutRequiresAToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
        verify(authService, never()).logout(any());
    }

    @Test
    void logoutRevokesTheTokenThatWasSent() throws Exception {
        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        verify(authService).logout(token);
    }

    // ── admin identity comes from the token ─────────────────

    @Test
    void issueUsesTheLoggedInAdminAndIgnoresAnySpoofedId() throws Exception {
        when(bookIssueService.issueBook(any(), any())).thenReturn(new BookIssueResponseDTO());

        mockMvc.perform(post("/api/book-issues/issue").header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"memberId\":\"MEM000001\",\"bookEditionId\":\"ED000001\","
                                + "\"issuedDate\":\"2026-07-01\",\"issuedById\":\"ADM999999\"}"))
                .andExpect(status().isCreated());

        verify(bookIssueService).issueBook(any(), eq("ADM000007"));
    }

    @Test
    void returnUsesTheLoggedInAdmin() throws Exception {
        when(bookIssueService.returnBook(any(), any())).thenReturn(new BookIssueResponseDTO());

        mockMvc.perform(post("/api/book-issues/return").header("Authorization", bearer(token)).contentType(JSON)
                        .content("{\"issueId\":\"BKI000001\",\"returnedDate\":\"2026-07-10\","
                                + "\"receivedById\":\"ADM999999\"}"))
                .andExpect(status().isOk());

        verify(bookIssueService).returnBook(any(), eq("ADM000007"));
    }

    @Test
    void markLostUsesTheLoggedInAdmin() throws Exception {
        when(bookIssueService.markBookAsLost(any(), any(), any())).thenReturn(new BookIssueResponseDTO());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/book-issues/BKI000001/lost").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        verify(bookIssueService).markBookAsLost(eq("BKI000001"), eq("ADM000007"), any());
    }

    // ── error mapping ───────────────────────────────────────

    @Test
    void unknownUrlIs404() throws Exception {
        mockMvc.perform(get("/api/does-not-exist").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void wrongMethodIs405() throws Exception {
        mockMvc.perform(delete("/api/members").header("Authorization", bearer(token)))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mockMvc.perform(post("/api/book-issues/issue").header("Authorization", bearer(token))
                        .contentType(JSON).content("{bad"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void wrongContentTypeIs415() throws Exception {
        mockMvc.perform(post("/api/book-issues/issue").header("Authorization", bearer(token))
                        .contentType(MediaType.TEXT_PLAIN).content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void missingRequiredParameterIs400() throws Exception {
        mockMvc.perform(get("/api/book-issues/can-issue").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required parameter: memberId"));
    }

    @Test
    void bodyValidationFailureIs400WithFieldDetails() throws Exception {
        mockMvc.perform(post("/api/book-issues/issue").header("Authorization", bearer(token))
                        .contentType(JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.memberId").exists());
    }
}
