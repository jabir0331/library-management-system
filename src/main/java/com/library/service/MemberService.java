package com.library.service;

import com.library.dto.request.MemberRequestDTO;
import com.library.dto.response.MemberResponseDTO;
import com.library.model.Member.MemberStatus;

import java.util.List;

public interface MemberService {

    MemberResponseDTO createMember(MemberRequestDTO request);

    MemberResponseDTO getMemberById(String memberId);

    MemberResponseDTO getMemberByEmail(String email);

    List<MemberResponseDTO> getAllMembers();

    List<MemberResponseDTO> getMembersByStatus(MemberStatus status);

    MemberResponseDTO updateMember(String memberId, MemberRequestDTO request);

    void deleteMember(String memberId);

    MemberResponseDTO updateMemberStatus(String memberId, MemberStatus status);

    List<MemberResponseDTO> searchMembers(String keyword);

    boolean existsByEmail(String email);
}