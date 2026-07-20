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

    // 1. Create Admin
    @PostMapping
    public ResponseEntity<AdminResponseDTO> createAdmin(@Valid @RequestBody AdminRequestDTO request) {
        AdminResponseDTO response = adminService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. Get Admin by ID
    @GetMapping("/{adminId}")
    public ResponseEntity<AdminResponseDTO> getAdminById(@PathVariable String adminId) {  // Changed to String
        AdminResponseDTO response = adminService.getAdminById(adminId);
        return ResponseEntity.ok(response);
    }

    // 3. Get Admin by Username
    @GetMapping("/username/{username}")
    public ResponseEntity<AdminResponseDTO> getAdminByUsername(@PathVariable String username) {
        AdminResponseDTO response = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(response);
    }

    // 4. Get Admin by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<AdminResponseDTO> getAdminByEmail(@PathVariable String email) {
        AdminResponseDTO response = adminService.getAdminByEmail(email);
        return ResponseEntity.ok(response);
    }

    // 5. Get All Admins
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        List<AdminResponseDTO> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    // 6. Update Admin
    @PutMapping("/{adminId}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @PathVariable String adminId,  // Changed to String
            @Valid @RequestBody AdminRequestDTO request) {
        AdminResponseDTO response = adminService.updateAdmin(adminId, request);
        return ResponseEntity.ok(response);
    }

    // 7. Delete Admin
    @DeleteMapping("/{adminId}")
    public ResponseEntity<Void> deleteAdmin(@PathVariable String adminId) {  // Changed to String
        adminService.deleteAdmin(adminId);
        return ResponseEntity.noContent().build();
    }

    // 8. Search Admins
    @GetMapping("/search")
    public ResponseEntity<List<AdminResponseDTO>> searchAdmins(@RequestParam String keyword) {
        List<AdminResponseDTO> admins = adminService.searchAdmins(keyword);
        return ResponseEntity.ok(admins);
    }

    // 9. Check if Username Exists
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = adminService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    // 10. Check if Email Exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = adminService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}