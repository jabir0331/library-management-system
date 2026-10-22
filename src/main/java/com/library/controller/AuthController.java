package com.library.controller;

import com.library.dto.request.LoginRequestDTO;
import com.library.dto.response.AuthResponseDTO;
import com.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Admin login (JWT)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Login", description = "Authenticates an admin and returns a JWT bearer token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authenticated, token returned"),
            @ApiResponse(responseCode = "400", description = "Username or password missing"),
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the Bearer token sent with this request")
    public ResponseEntity<Map<String, String>> logout(
            @RequestHeader("Authorization") String authorization) {
        // The security filter already guaranteed this is a valid "Bearer <token>" header
        authService.logout(authorization.substring(7));
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
