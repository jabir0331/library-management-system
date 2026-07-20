package com.library.service;

import com.library.dto.request.AdminRequestDTO;
import com.library.dto.response.AdminResponseDTO;

import java.util.List;

public interface AdminService {

    AdminResponseDTO createAdmin(AdminRequestDTO request);

    AdminResponseDTO getAdminById(String id);  // Changed Long to String

    AdminResponseDTO getAdminByUsername(String username);

    AdminResponseDTO getAdminByEmail(String email);

    List<AdminResponseDTO> getAllAdmins();

    AdminResponseDTO updateAdmin(String id, AdminRequestDTO request);  // Changed Long to String

    void deleteAdmin(String id);  // Changed Long to String

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<AdminResponseDTO> searchAdmins(String keyword);
}