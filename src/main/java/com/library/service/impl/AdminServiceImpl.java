package com.library.service.impl;

import com.library.dto.request.AdminRequestDTO;
import com.library.dto.response.AdminResponseDTO;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Admin;
import com.library.repository.AdminRepository;
import com.library.service.AdminService;
import com.library.util.AdminIdGenerator;  // NEW import
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepository;
    private final ModelMapper modelMapper;
    private final AdminIdGenerator adminIdGenerator;  // NEW dependency

    @Override
    public AdminResponseDTO createAdmin(AdminRequestDTO request) {
        // Validate username uniqueness
        if (adminRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists: " + request.getUsername());
        }

        // Validate email uniqueness
        if (adminRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered: " + request.getEmail());
        }

        // Convert DTO to Entity
        Admin admin = modelMapper.map(request, Admin.class);

        // Generate custom ID
        String lastId = adminRepository.findLastAdminId();
        admin.setAdminId(adminIdGenerator.generateNextId(lastId));

        // Save to database
        Admin savedAdmin = adminRepository.save(admin);

        // Convert Entity to Response DTO
        return modelMapper.map(savedAdmin, AdminResponseDTO.class);
    }

    @Override
    public AdminResponseDTO getAdminById(String id) {  // Changed Long to String
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", id));
        return modelMapper.map(admin, AdminResponseDTO.class);
    }

    @Override
    public AdminResponseDTO getAdminByUsername(String username) {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with username: " + username));
        return modelMapper.map(admin, AdminResponseDTO.class);
    }

    @Override
    public AdminResponseDTO getAdminByEmail(String email) {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with email: " + email));
        return modelMapper.map(admin, AdminResponseDTO.class);
    }

    @Override
    public List<AdminResponseDTO> getAllAdmins() {
        return adminRepository.findAll()
                .stream()
                .map(admin -> modelMapper.map(admin, AdminResponseDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public AdminResponseDTO updateAdmin(String id, AdminRequestDTO request) {  // Changed Long to String
        // Find existing admin
        Admin existingAdmin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", id));

        // Check if new username conflicts (if changed)
        if (!existingAdmin.getUsername().equals(request.getUsername())
                && adminRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username already taken: " + request.getUsername());
        }

        // Check if new email conflicts (if changed)
        if (!existingAdmin.getEmail().equals(request.getEmail())
                && adminRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered: " + request.getEmail());
        }

        // Update fields
        existingAdmin.setUsername(request.getUsername());
        existingAdmin.setEmail(request.getEmail());
        existingAdmin.setPassword(request.getPassword()); // TODO: Encrypt later

        // Save updated admin
        Admin updatedAdmin = adminRepository.save(existingAdmin);

        // Convert to Response DTO
        return modelMapper.map(updatedAdmin, AdminResponseDTO.class);
    }

    @Override
    public void deleteAdmin(String id) {  // Changed Long to String
        if (!adminRepository.existsById(id)) {
            throw new ResourceNotFoundException("Admin", id);
        }
        adminRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return adminRepository.existsByEmail(email);
    }

    @Override
    public List<AdminResponseDTO> searchAdmins(String keyword) {
        return adminRepository.searchAdmins(keyword)
                .stream()
                .map(admin -> modelMapper.map(admin, AdminResponseDTO.class))
                .collect(Collectors.toList());
    }
}