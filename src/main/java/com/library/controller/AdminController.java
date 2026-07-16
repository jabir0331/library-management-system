package com.library.controller;

import com.library.dto.request.AdminRequestDTO;
import com.library.dto.response.AdminResponseDTO;
import com.library.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // Create Admin
    @PostMapping
    public ResponseEntity<AdminResponseDTO> createAdmin(@Valid @RequestBody AdminRequestDTO request) {
        AdminResponseDTO response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Admin by ID
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdminById(@PathVariable Long id) {
        AdminResponseDTO response = adminService.getAdminById(id);
        return ResponseEntity.ok(response);
    }

    // Get Admin by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<AdminResponseDTO> getAdminByUsername(@PathVariable String username) {
        AdminResponseDTO response = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(response);
    }

    // Get Admin by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<AdminResponseDTO> getAdminByEmail(@PathVariable String email) {
        AdminResponseDTO response = adminService.getAdminByEmail(email);
        return ResponseEntity.ok(response);
    }

    // Get All Admins
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        List<AdminResponseDTO> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    // Update Admin
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminRequestDTO request) {
        AdminResponseDTO response = adminService.updateAdmin(id, request);
        return ResponseEntity.ok(response);
    }

    // Delete Admin
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.noContent().build();
    }

    // Search Admins
    @GetMapping("/search")
    public ResponseEntity<List<AdminResponseDTO>> searchAdmins(@RequestParam String keyword) {
        List<AdminResponseDTO> admins = adminService.searchAdmins(keyword);
        return ResponseEntity.ok(admins);
    }

    // Check if Username Exists
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = adminService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    // Check if Email Exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = adminService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}