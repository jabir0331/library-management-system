package com.library.service.impl;

import com.library.dto.request.MemberRequestDTO;
import com.library.dto.response.MemberResponseDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Member;
import com.library.model.Member.MemberStatus;
import com.library.repository.MemberRepository;
import com.library.service.MemberService;
import com.library.util.MemberIdGenerator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final MemberIdGenerator idGenerator;

    @Override
    public MemberResponseDTO createMember(MemberRequestDTO request) {
        // 1. Validate email uniqueness
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        // 2. Convert DTO to Entity
        Member member = modelMapper.map(request, Member.class);

        // 3. Generate custom member ID
        String lastId = memberRepository.findLastMemberId();
        String newId = idGenerator.generateNextId(lastId);
        member.setMemberId(newId);

        // 4. Set default status
        member.setStatus(MemberStatus.ACTIVE);

        // 5. Save to database
        Member savedMember = memberRepository.save(member);

        // 6. Convert to Response DTO
        return mapToResponseDTO(savedMember);
    }

    @Override
    public MemberResponseDTO getMemberById(String memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));
        return mapToResponseDTO(member);
    }

    @Override
    public MemberResponseDTO getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found with email: " + email));
        return mapToResponseDTO(member);
    }

    @Override
    public List<MemberResponseDTO> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberResponseDTO> getMembersByStatus(MemberStatus status) {
        return memberRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MemberResponseDTO updateMember(String memberId, MemberRequestDTO request) {
        Member existingMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        if (!existingMember.getEmail().equals(request.getEmail())
                && memberRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered: " + request.getEmail());
        }

        existingMember.setFirstName(request.getFirstName());
        existingMember.setLastName(request.getLastName());
        existingMember.setEmail(request.getEmail());
        existingMember.setAddress(request.getAddress());

        Member updatedMember = memberRepository.save(existingMember);
        return mapToResponseDTO(updatedMember);
    }

    @Override
    public void deleteMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        memberRepository.deleteById(memberId);
    }

    @Override
    public MemberResponseDTO updateMemberStatus(String memberId, MemberStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        member.setStatus(status);
        Member updatedMember = memberRepository.save(member);

        return mapToResponseDTO(updatedMember);
    }

    @Override
    public List<MemberResponseDTO> searchMembers(String keyword) {
        return memberRepository.searchMembers(keyword)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    private MemberResponseDTO mapToResponseDTO(Member member) {
        MemberResponseDTO response = modelMapper.map(member, MemberResponseDTO.class);
        response.setFullName(member.getFullName());
        response.setStatus(member.getStatus().name());
        return response;
    }
}