package com.library.controller;

import com.library.dto.request.MemberRequestDTO;
import com.library.dto.response.MemberResponseDTO;
import com.library.model.Member.MemberStatus;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // Create Member
    @PostMapping
    public ResponseEntity<MemberResponseDTO> createMember(@Valid @RequestBody MemberRequestDTO request) {
        MemberResponseDTO response = memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Member by ID (MEM000001)
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDTO> getMemberById(@PathVariable String memberId) {
        MemberResponseDTO response = memberService.getMemberById(memberId);
        return ResponseEntity.ok(response);
    }

    // Get Member by Email
    @GetMapping("/email/{email}")
    public ResponseEntity<MemberResponseDTO> getMemberByEmail(@PathVariable String email) {
        MemberResponseDTO response = memberService.getMemberByEmail(email);
        return ResponseEntity.ok(response);
    }

    // 4. Get All Members
    @GetMapping
    public ResponseEntity<List<MemberResponseDTO>> getAllMembers() {
        List<MemberResponseDTO> members = memberService.getAllMembers();
        return ResponseEntity.ok(members);
    }

    // 5. Get Members by Status
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MemberResponseDTO>> getMembersByStatus(@PathVariable MemberStatus status) {
        List<MemberResponseDTO> members = memberService.getMembersByStatus(status);
        return ResponseEntity.ok(members);
    }

    // 6. Update Member
    @PutMapping("/{memberId}")
    public ResponseEntity<MemberResponseDTO> updateMember(
            @PathVariable String memberId,
            @Valid @RequestBody MemberRequestDTO request) {
        MemberResponseDTO response = memberService.updateMember(memberId, request);
        return ResponseEntity.ok(response);
    }

    // 7. Delete Member
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteMember(@PathVariable String memberId) {
        memberService.deleteMember(memberId);
        return ResponseEntity.noContent().build();
    }

    // 8. Update Member Status
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<MemberResponseDTO> updateMemberStatus(
            @PathVariable String memberId,
            @RequestParam MemberStatus status) {
        MemberResponseDTO response = memberService.updateMemberStatus(memberId, status);
        return ResponseEntity.ok(response);
    }

    // 9. Search Members
    @GetMapping("/search")
    public ResponseEntity<List<MemberResponseDTO>> searchMembers(@RequestParam String keyword) {
        List<MemberResponseDTO> members = memberService.searchMembers(keyword);
        return ResponseEntity.ok(members);
    }

    // 10. Check if Email Exists
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = memberService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }
}