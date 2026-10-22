package com.library.service;

import com.library.dto.request.AdminRequestDTO;
import com.library.dto.response.AdminResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {

    AdminResponseDTO createAdmin(AdminRequestDTO request);

    AdminResponseDTO getAdminById(String id);  // Changed Long to String

    AdminResponseDTO getAdminByUsername(String username);

    AdminResponseDTO getAdminByEmail(String email);

    Page<AdminResponseDTO> getAllAdmins(Pageable pageable);

    AdminResponseDTO updateAdmin(String id, AdminRequestDTO request);  // Changed Long to String

    void deleteAdmin(String id);  // Changed Long to String

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Page<AdminResponseDTO> searchAdmins(String keyword, Pageable pageable);
}