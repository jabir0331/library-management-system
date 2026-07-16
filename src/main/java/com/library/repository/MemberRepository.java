package com.library.repository;

import com.library.model.Member;
import com.library.model.Member.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    // Find by email
    Optional<Member> findByEmail(String email);

    // Find by status
    List<Member> findByStatus(MemberStatus status);

    // Check if email exists
    boolean existsByEmail(String email);

    // Search by first name or last name
    @Query("SELECT m FROM Member m WHERE LOWER(m.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Member> searchMembers(@Param("keyword") String keyword);

    // Find active members
    @Query("SELECT m FROM Member m WHERE m.status = :status")
    List<Member> findMembersByStatus(@Param("status") MemberStatus status);

    // Get the last member ID (for generating next ID)
    @Query("SELECT m.memberId FROM Member m ORDER BY m.memberId DESC LIMIT 1")
    String findLastMemberId();

    // Find members by full name (first + last)
    @Query("SELECT m FROM Member m WHERE LOWER(m.firstName) = LOWER(:firstName) AND LOWER(m.lastName) = LOWER(:lastName)")
    List<Member> findByFirstNameAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);
}