package com.library.service;

import com.library.dto.request.AdminRequestDTO;
import com.library.dto.response.AdminResponseDTO;

import java.util.List;

public interface AdminService {

    AdminResponseDTO createAdmin(AdminRequestDTO request);

    AdminResponseDTO getAdminById(Long id);

    AdminResponseDTO getAdminByUsername(String username);

    AdminResponseDTO getAdminByEmail(String email);

    List<AdminResponseDTO> getAllAdmins();

    AdminResponseDTO updateAdmin(Long id, AdminRequestDTO request);

    void deleteAdmin(Long id);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<AdminResponseDTO> searchAdmins(String keyword);
}