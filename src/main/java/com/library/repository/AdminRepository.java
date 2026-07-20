package com.library.repository;

import com.library.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {  // Changed Long to String

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT a FROM Admin a WHERE a.username LIKE %:keyword% OR a.email LIKE %:keyword%")
    List<Admin> searchAdmins(@Param("keyword") String keyword);

    @Query(value = "SELECT * FROM admin WHERE created_at > NOW() - INTERVAL '30 days'", nativeQuery = true)
    List<Admin> findRecentAdmins();

    // NEW: Get last admin ID for generation
    @Query("SELECT a.adminId FROM Admin a ORDER BY a.adminId DESC LIMIT 1")
    String findLastAdminId();
}