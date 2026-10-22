package com.library.service;

import com.library.dto.request.MemberRequestDTO;
import com.library.dto.response.MemberResponseDTO;
import com.library.model.Member.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    MemberResponseDTO createMember(MemberRequestDTO request);

    MemberResponseDTO getMemberById(String memberId);

    MemberResponseDTO getMemberByEmail(String email);

    Page<MemberResponseDTO> getAllMembers(Pageable pageable);

    Page<MemberResponseDTO> getMembersByStatus(MemberStatus status, Pageable pageable);

    MemberResponseDTO updateMember(String memberId, MemberRequestDTO request);

    void deleteMember(String memberId);

    MemberResponseDTO updateMemberStatus(String memberId, MemberStatus status);

    Page<MemberResponseDTO> searchMembers(String keyword, Pageable pageable);

    boolean existsByEmail(String email);
}