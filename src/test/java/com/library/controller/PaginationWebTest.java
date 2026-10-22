package com.library.controller;

import com.library.dto.response.BookIssueResponseDTO;
import com.library.dto.response.MemberResponseDTO;
import com.library.model.Admin;
import com.library.model.enums.IssueStatus;
import com.library.repository.AdminRepository;
import com.library.repository.RevokedTokenRepository;
import com.library.security.AdminUserDetailsService;
import com.library.security.JwtAuthenticationFilter;
import com.library.security.JwtService;
import com.library.security.SecurityConfig;
import com.library.service.BookIssueService;
import com.library.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the paging contract: JSON shape, defaults, page-size cap and sorting arguments. */
@WebMvcTest(controllers = {MemberController.class, BookIssueController.class},
        properties = {
                "app.jwt.secret=web-test-secret-web-test-secret-123456",
                "app.jwt.expiration-ms=3600000",
                "spring.data.web.pageable.max-page-size=100"
        })
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, JwtService.class, AdminUserDetailsService.class})
class PaginationWebTest {

    @Autowired MockMvc mockMvc;
    @Autowired JwtService jwtService;

    @MockitoBean MemberService memberService;
    @MockitoBean BookIssueService bookIssueService;
    @MockitoBean AdminRepository adminRepository;
    @MockitoBean RevokedTokenRepository revokedTokenRepository;

    private String auth;

    @BeforeEach
    void setUp() {
        Admin admin = new Admin();
        admin.setAdminId("ADM000001");
        admin.setUsername("librarian");
        admin.setPassword("$2a$10$irrelevant");
        when(adminRepository.findByUsername("librarian")).thenReturn(Optional.of(admin));
        auth = "Bearer " + jwtService.generateToken("librarian", "ADM000001");
    }

    private Pageable capturedMemberPageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(memberService).getAllMembers(captor.capture());
        return captor.getValue();
    }

    @Test
    void responseIsAPageEnvelopeWithMetadata() throws Exception {
        MemberResponseDTO member = new MemberResponseDTO();
        member.setMemberId("MEM000021");
        Page<MemberResponseDTO> page = new PageImpl<>(List.of(member), PageRequest.of(1, 20), 21);
        when(memberService.getAllMembers(any())).thenReturn(page);

        mockMvc.perform(get("/api/members").param("page", "1").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].memberId").value("MEM000021"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(21))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void defaultsToTwentyItemsSortedById() throws Exception {
        when(memberService.getAllMembers(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/members").header("Authorization", auth)).andExpect(status().isOk());

        Pageable pageable = capturedMemberPageable();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).isEqualTo(Sort.by("memberId"));
    }

    @Test
    void passesPageSizeAndSortFromTheRequest() throws Exception {
        when(memberService.getAllMembers(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/members").header("Authorization", auth)
                        .param("page", "3").param("size", "5").param("sort", "firstName,desc"))
                .andExpect(status().isOk());

        Pageable pageable = capturedMemberPageable();
        assertThat(pageable.getPageNumber()).isEqualTo(3);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Order.desc("firstName")));
    }

    @Test
    void pageSizeIsCappedAtOneHundred() throws Exception {
        when(memberService.getAllMembers(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/members").header("Authorization", auth).param("size", "5000"))
                .andExpect(status().isOk());

        assertThat(capturedMemberPageable().getPageSize()).isEqualTo(100);
    }

    @Test
    void issuesDefaultToNewestFirst() throws Exception {
        when(bookIssueService.getAllIssues(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/book-issues").header("Authorization", auth)).andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookIssueService).getAllIssues(captor.capture());
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Order.desc("issueId")));
    }

    @Test
    void issuesCanBeFilteredByStatusWithPaging() throws Exception {
        BookIssueResponseDTO issue = new BookIssueResponseDTO();
        issue.setIssueId("BKI000007");
        when(bookIssueService.getIssuesByStatus(eq(IssueStatus.LOST), any()))
                .thenReturn(new PageImpl<>(List.of(issue)));

        mockMvc.perform(get("/api/book-issues/status/LOST").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].issueId").value("BKI000007"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void unknownStatusIsABadRequest() throws Exception {
        mockMvc.perform(get("/api/book-issues/status/NOPE").header("Authorization", auth))
                .andExpect(status().isBadRequest());
    }
}
