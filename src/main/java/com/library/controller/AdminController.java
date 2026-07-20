package com.library.controller;

import com.library.dto.request.AdminRequestDTO;
import com.library.dto.response.AdminResponseDTO;
import com.library.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "Endpoints for managing library administrators")
public class AdminController {

    private final AdminService adminService;

    // 1. Create Admin
    @PostMapping
    @Operation(
            summary = "Create a new Admin",
            description = "Creates a new administrator with auto-generated ID (ADM000001 format)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Admin created successfully",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Username or email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Username already exists: admin1\"}")
                    )
            )
    })
    public ResponseEntity<AdminResponseDTO> createAdmin(
            @Parameter(description = "Admin details to create", required = true)
            @Valid @RequestBody AdminRequestDTO request) {
        AdminResponseDTO response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Get Admin by ID
    @GetMapping("/{adminId}")
    @Operation(
            summary = "Get Admin by ID",
            description = "Retrieves admin details by their unique ID (e.g., ADM000001)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin found",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Admin not found with id: ADM999999\"}")
                    )
            )
    })
    public ResponseEntity<AdminResponseDTO> getAdminById(
            @Parameter(description = "Admin ID (e.g., ADM000001)", required = true, example = "ADM000001")
            @PathVariable String adminId) {
        AdminResponseDTO response = adminService.getAdminById(adminId);
        return ResponseEntity.ok(response);
    }

    // 3. Get Admin by Username
    @GetMapping("/username/{username}")
    @Operation(
            summary = "Get Admin by Username",
            description = "Retrieves admin details by their username"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin found",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Admin not found with username: non_existent\"}")
                    )
            )
    })
    public ResponseEntity<AdminResponseDTO> getAdminByUsername(
            @Parameter(description = "Username of the admin", required = true, example = "superadmin")
            @PathVariable String username) {
        AdminResponseDTO response = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(response);
    }

    // 4. Get Admin by Email
    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get Admin by Email",
            description = "Retrieves admin details by their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin found",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Admin not found with email: non_existent@email.com\"}")
                    )
            )
    })
    public ResponseEntity<AdminResponseDTO> getAdminByEmail(
            @Parameter(description = "Email of the admin", required = true, example = "superadmin@library.com")
            @PathVariable String email) {
        AdminResponseDTO response = adminService.getAdminByEmail(email);
        return ResponseEntity.ok(response);
    }

    // 5. Get All Admins
    @GetMapping
    @Operation(
            summary = "Get All Admins",
            description = "Retrieves a list of all administrators"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of admins retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            )
    })
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        List<AdminResponseDTO> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    // 6. Update Admin
    @PutMapping("/{adminId}")
    @Operation(
            summary = "Update Admin",
            description = "Updates an existing admin's details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Admin updated successfully",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Username or email already taken",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":400,\"error\":\"Business Rule Violation\",\"message\":\"Username already taken: admin1\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Admin not found with id: ADM999999\"}")
                    )
            )
    })
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @Parameter(description = "Admin ID (e.g., ADM000001)", required = true, example = "ADM000001")
            @PathVariable String adminId,
            @Parameter(description = "Updated admin details", required = true)
            @Valid @RequestBody AdminRequestDTO request) {
        AdminResponseDTO response = adminService.updateAdmin(adminId, request);
        return ResponseEntity.ok(response);
    }

    // 7. Delete Admin
    @DeleteMapping("/{adminId}")
    @Operation(
            summary = "Delete Admin",
            description = "Permanently deletes an admin by their ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Admin deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Admin not found",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"timestamp\":\"2026-07-20T10:00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"Admin not found with id: ADM999999\"}")
                    )
            )
    })
    public ResponseEntity<Void> deleteAdmin(
            @Parameter(description = "Admin ID (e.g., ADM000001)", required = true, example = "ADM000001")
            @PathVariable String adminId) {
        adminService.deleteAdmin(adminId);
        return ResponseEntity.noContent().build();
    }

    // 8. Search Admins
    @GetMapping("/search")
    @Operation(
            summary = "Search Admins",
            description = "Searches admins by username or email (partial match)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Search results retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            )
    })
    public ResponseEntity<List<AdminResponseDTO>> searchAdmins(
            @Parameter(description = "Search keyword (matches username or email)", required = true, example = "admin")
            @RequestParam String keyword) {
        List<AdminResponseDTO> admins = adminService.searchAdmins(keyword);
        return ResponseEntity.ok(admins);
    }

    // 9. Check if Username Exists
    @GetMapping("/exists/username/{username}")
    @Operation(
            summary = "Check if Username Exists",
            description = "Checks if a username is already taken"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns true if username exists, false otherwise",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> existsByUsername(
            @Parameter(description = "Username to check", required = true, example = "superadmin")
            @PathVariable String username) {
        boolean exists = adminService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    // 10. Check if Email Exists
    @GetMapping("/exists/email/{email}")
    @Operation(
            summary = "Check if Email Exists",
            description = "Checks if an email is already registered"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Returns true if email exists, false otherwise",
                    content = @Content(schema = @Schema(implementation = Boolean.class))
            )
    })
    public ResponseEntity<Boolean> existsByEmail(
            @Parameter(description = "Email to check", required = true, example = "superadmin@library.com")
            @PathVariable String email) {
        boolean exists = adminService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}