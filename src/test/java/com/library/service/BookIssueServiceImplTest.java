package com.library.service;

import com.library.dto.request.BookIssueRequestDTO;
import com.library.dto.request.BookReturnRequestDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.exception.BusinessException;
import com.library.exception.ResourceNotFoundException;
import com.library.exception.ValidationException;
import com.library.model.Admin;
import com.library.model.Book;
import com.library.model.BookEdition;
import com.library.model.BookIssue;
import com.library.model.Member;
import com.library.model.enums.IssueStatus;
import com.library.model.enums.ReturnCondition;
import com.library.repository.AdminRepository;
import com.library.repository.BookEditionRepository;
import com.library.repository.BookIssueRepository;
import com.library.repository.MemberRepository;
import com.library.service.impl.BookIssueServiceImpl;
import com.library.util.BookIssueIdGenerator;
import com.library.util.BookIssueReferenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookIssueServiceImplTest {

    private static final LocalDate ISSUED = LocalDate.of(2026, 7, 1);
    private static final LocalDate DUE = LocalDate.of(2026, 7, 15);

    @Mock BookIssueRepository bookIssueRepository;
    @Mock MemberRepository memberRepository;
    @Mock BookEditionRepository bookEditionRepository;
    @Mock AdminRepository adminRepository;
    @Mock SystemConfigService configService;
    @Spy BookIssueIdGenerator issueIdGenerator = new BookIssueIdGenerator();
    @Spy BookIssueReferenceGenerator referenceGenerator = new BookIssueReferenceGenerator();

    @InjectMocks BookIssueServiceImpl service;

    private Member member;
    private BookEdition edition;
    private Admin admin;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setMemberId("MEM000001");
        member.setFirstName("Ada");
        member.setLastName("Lovelace");
        member.setStatus(Member.MemberStatus.ACTIVE);

        Book book = new Book();
        book.setBookId("BK000001");
        book.setTitle("Clean Code");

        edition = new BookEdition();
        edition.setEditionId("ED000001");
        edition.setBook(book);
        edition.setIsbn("9780132350884");
        edition.setTotalCopies(5);
        edition.setAvailableCopies(5);
        edition.setPrice(new BigDecimal("2000.00"));

        admin = new Admin();
        admin.setAdminId("ADM000001");
        admin.setUsername("librarian");

        when(memberRepository.findById("MEM000001")).thenReturn(Optional.of(member));
        when(bookEditionRepository.findById("ED000001")).thenReturn(Optional.of(edition));
        when(adminRepository.findById("ADM000001")).thenReturn(Optional.of(admin));
        when(bookIssueRepository.save(any(BookIssue.class))).thenAnswer(i -> i.getArgument(0));
        when(bookIssueRepository.findLastIssueId()).thenReturn("BKI000009");
        when(bookIssueRepository.countActiveIssuesByMember("MEM000001")).thenReturn(0L);

        when(configService.getMaxBooksPerMember()).thenReturn(3);
        when(configService.getMaxIssueDays()).thenReturn(14);
        when(configService.getFinePerDay()).thenReturn(new BigDecimal("25.00"));
        when(configService.getGracePeriodDays()).thenReturn(0);
        when(configService.getLostBookMultiplier()).thenReturn(new BigDecimal("1.5"));
        when(configService.getServiceCharge()).thenReturn(new BigDecimal("250.00"));
    }

    private BookIssueRequestDTO issueRequest() {
        BookIssueRequestDTO request = new BookIssueRequestDTO();
        request.setMemberId("MEM000001");
        request.setBookEditionId("ED000001");
        request.setIssuedDate(ISSUED);
        return request;
    }

    private BookIssue openIssue() {
        BookIssue issue = new BookIssue();
        issue.setIssueId("BKI000001");
        issue.setReferenceNumber("BKI-20260701-100000-ABCD");
        issue.setMember(member);
        issue.setBookEdition(edition);
        issue.setIssuedBy(admin);
        issue.setIssuedDate(ISSUED);
        issue.setDueDate(DUE);
        issue.setStatus(IssueStatus.ISSUED);
        edition.setAvailableCopies(4); // one copy is out
        when(bookIssueRepository.findById("BKI000001")).thenReturn(Optional.of(issue));
        return issue;
    }

    private BookReturnRequestDTO returnRequest(LocalDate returnedOn) {
        BookReturnRequestDTO request = new BookReturnRequestDTO();
        request.setIssueId("BKI000001");
        request.setReturnedDate(returnedOn);
        return request;
    }

    @Nested
    class IssueBook {

        @Test
        void issuesBookAndRecordsAdminAndDueDateFromConfig() {
            BookIssueRequestDTO request = issueRequest();
            request.setIssuedDate(LocalDate.now());

            BookIssueResponseDTO response = service.issueBook(request, "ADM000001");

            assertThat(response.getIssueId()).isEqualTo("BKI000010");
            assertThat(response.getStatus()).isEqualTo(IssueStatus.ISSUED);
            assertThat(response.getIssuedBy()).isEqualTo("librarian");
            assertThat(response.getDueDate()).isEqualTo(LocalDate.now().plusDays(14));
            assertThat(response.getMemberName()).isEqualTo("Ada Lovelace");
            assertThat(response.getBookTitle()).isEqualTo("Clean Code");
            assertThat(edition.getAvailableCopies()).isEqualTo(4);
        }

        @Test
        void usesTheProvidedDueDate() {
            BookIssueRequestDTO request = issueRequest();
            request.setDueDate(ISSUED.plusDays(3));

            BookIssueResponseDTO response = service.issueBook(request, "ADM000001");

            assertThat(response.getDueDate()).isEqualTo(ISSUED.plusDays(3));
        }

        @Test
        void rejectsDueDateThatIsNotAfterIssueDate() {
            BookIssueRequestDTO request = issueRequest();
            request.setDueDate(ISSUED);

            assertThatThrownBy(() -> service.issueBook(request, "ADM000001"))
                    .isInstanceOf(ValidationException.class);
            verify(bookIssueRepository, never()).save(any());
        }

        @Test
        void rejectsUnknownMember() {
            when(memberRepository.findById("MEM000001")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.issueBook(issueRequest(), "ADM000001"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        void rejectsInactiveMember() {
            member.setStatus(Member.MemberStatus.SUSPENDED);

            assertThatThrownBy(() -> service.issueBook(issueRequest(), "ADM000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("not active");
            verify(bookIssueRepository, never()).save(any());
        }

        @Test
        void rejectsMemberWhoHasOverdueBooks() {
            when(bookIssueRepository.countOverdueIssuesByMember(eq("MEM000001"), any(LocalDate.class)))
                    .thenReturn(2L);

            assertThatThrownBy(() -> service.issueBook(issueRequest(), "ADM000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("2 overdue book(s)");
            assertThat(edition.getAvailableCopies()).isEqualTo(5);
            verify(bookIssueRepository, never()).save(any());
        }

        @Test
        void rejectsWhenNoCopiesAvailable() {
            edition.setAvailableCopies(0);

            assertThatThrownBy(() -> service.issueBook(issueRequest(), "ADM000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("No copies available");
            verify(bookIssueRepository, never()).save(any());
        }

        @Test
        void rejectsWhenMemberReachedBorrowLimit() {
            when(bookIssueRepository.countActiveIssuesByMember("MEM000001")).thenReturn(3L);

            assertThatThrownBy(() -> service.issueBook(issueRequest(), "ADM000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("maximum book limit");
            assertThat(edition.getAvailableCopies()).isEqualTo(5);
            verify(bookIssueRepository, never()).save(any());
        }

        @Test
        void rejectsUnknownAdmin() {
            assertThatThrownBy(() -> service.issueBook(issueRequest(), "ADM999999"))
                    .isInstanceOf(ResourceNotFoundException.class);
            assertThat(edition.getAvailableCopies()).isEqualTo(5);
        }
    }

    @Nested
    class ReturnBook {

        @Test
        void returnOnDueDateHasNoFineAndRestoresCopy() {
            BookIssue issue = openIssue();

            BookIssueResponseDTO response = service.returnBook(returnRequest(DUE), "ADM000001");

            assertThat(response.getStatus()).isEqualTo(IssueStatus.RETURNED);
            assertThat(issue.getFineAmount()).isEqualByComparingTo("0");
            assertThat(issue.getReceivedBy()).isSameAs(admin);
            assertThat(issue.getReturnCondition()).isEqualTo(ReturnCondition.GOOD);
            assertThat(edition.getAvailableCopies()).isEqualTo(5);
        }

        @Test
        void lateReturnIsStillReturnedAndCarriesTheFine() {
            BookIssue issue = openIssue();

            BookIssueResponseDTO response = service.returnBook(returnRequest(DUE.plusDays(5)), "ADM000001");

            assertThat(response.getStatus()).isEqualTo(IssueStatus.RETURNED);
            assertThat(issue.getStatus()).isEqualTo(IssueStatus.RETURNED);
            assertThat(response.isOverdue()).isTrue(); // it was returned late
            assertThat(issue.getFineAmount()).isEqualByComparingTo("125.00"); // 5 days x 25.00
        }

        @Test
        void gracePeriodIsSubtractedFromFinableDays() {
            when(configService.getGracePeriodDays()).thenReturn(2);
            BookIssue issue = openIssue();

            service.returnBook(returnRequest(DUE.plusDays(5)), "ADM000001");

            assertThat(issue.getFineAmount()).isEqualByComparingTo("75.00"); // (5 - 2) x 25.00
        }

        @Test
        void returnWithinGracePeriodHasNoFine() {
            when(configService.getGracePeriodDays()).thenReturn(2);
            BookIssue issue = openIssue();

            service.returnBook(returnRequest(DUE.plusDays(2)), "ADM000001");

            assertThat(issue.getFineAmount()).isEqualByComparingTo("0");
        }

        @Test
        void recordsTheReturnCondition() {
            BookIssue issue = openIssue();
            BookReturnRequestDTO request = returnRequest(DUE);
            request.setReturnCondition(ReturnCondition.DAMAGED);

            service.returnBook(request, "ADM000001");

            assertThat(issue.getReturnCondition()).isEqualTo(ReturnCondition.DAMAGED);
        }

        @Test
        void cannotReturnTwice() {
            BookIssue issue = openIssue();
            issue.setReturnedDate(DUE);
            issue.setStatus(IssueStatus.RETURNED);

            assertThatThrownBy(() -> service.returnBook(returnRequest(DUE), "ADM000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already returned");
        }

        @Test
        void cannotReturnALostBook() {
            BookIssue issue = openIssue();
            issue.setStatus(IssueStatus.LOST);

            assertThatThrownBy(() -> service.returnBook(returnRequest(DUE), "ADM000001"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("lost");
        }

        @Test
        void returnDateCannotPrecedeIssueDate() {
            openIssue();

            assertThatThrownBy(() -> service.returnBook(returnRequest(ISSUED.minusDays(1)), "ADM000001"))
                    .isInstanceOf(ValidationException.class);
            assertThat(edition.getAvailableCopies()).isEqualTo(4);
        }

        @Test
        void unknownIssueIsNotFound() {
            assertThatThrownBy(() -> service.returnBook(returnRequest(DUE), "ADM000001"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class MarkBookAsLost {

        @Test
        void autoPenaltyIsPriceTimesMultiplierPlusServiceCharge() {
            BookIssue issue = openIssue();

            BookIssueResponseDTO response = service.markBookAsLost("BKI000001", "ADM000001", null);

            assertThat(response.getStatus()).isEqualTo(IssueStatus.LOST);
            assertThat(issue.getLostBookPenalty()).isEqualByComparingTo("3250.00"); // 2000 x 1.5 + 250
            assertThat(issue.getReturnCondition()).isEqualTo(ReturnCondition.LOST);
            assertThat(issue.getReceivedBy()).isSameAs(admin);
        }

        @Test
        void customPenaltyOverridesTheCalculation() {
            BookIssue issue = openIssue();

            service.markBookAsLost("BKI000001", "ADM000001", new BigDecimal("999.99"));

            assertThat(issue.getLostBookPenalty()).isEqualByComparingTo("999.99");
        }

        @Test
        void lostCopyReducesTotalButNotAvailableAgain() {
            // Regression: available was already decremented at issue time
            openIssue(); // total 5, available 4

            service.markBookAsLost("BKI000001", "ADM000001", null);

            assertThat(edition.getTotalCopies()).isEqualTo(4);
            assertThat(edition.getAvailableCopies()).isEqualTo(4);
        }

        @Test
        void cannotMarkReturnedBookAsLost() {
            BookIssue issue = openIssue();
            issue.setReturnedDate(DUE);

            assertThatThrownBy(() -> service.markBookAsLost("BKI000001", "ADM000001", null))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void cannotMarkAsLostTwice() {
            BookIssue issue = openIssue();
            issue.setStatus(IssueStatus.LOST);

            assertThatThrownBy(() -> service.markBookAsLost("BKI000001", "ADM000001", null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("already marked as lost");
        }

        @Test
        void unknownAdminIsNotFound() {
            openIssue();

            assertThatThrownBy(() -> service.markBookAsLost("BKI000001", "ADM999999", null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class CanIssueBook {

        @Test
        void trueForActiveMemberUnderLimitWithCopies() {
            assertThat(service.canIssueBook("MEM000001", "ED000001")).isTrue();
        }

        @Test
        void falseForInactiveMember() {
            member.setStatus(Member.MemberStatus.INACTIVE);
            assertThat(service.canIssueBook("MEM000001", "ED000001")).isFalse();
        }

        @Test
        void falseWhenMemberHasOverdueBooks() {
            when(bookIssueRepository.countOverdueIssuesByMember(eq("MEM000001"), any(LocalDate.class)))
                    .thenReturn(1L);
            assertThat(service.canIssueBook("MEM000001", "ED000001")).isFalse();
        }

        @Test
        void falseAtBorrowLimit() {
            when(bookIssueRepository.countActiveIssuesByMember("MEM000001")).thenReturn(3L);
            assertThat(service.canIssueBook("MEM000001", "ED000001")).isFalse();
        }

        @Test
        void falseWhenNoCopiesLeft() {
            edition.setAvailableCopies(0);
            assertThat(service.canIssueBook("MEM000001", "ED000001")).isFalse();
        }
    }

    @Nested
    class Listing {

        @Test
        void lostIssuesAreMappedAndPaged() {
            BookIssue issue = openIssue();
            issue.setStatus(IssueStatus.LOST);
            Pageable pageable = PageRequest.of(0, 10);
            when(bookIssueRepository.findByStatus(IssueStatus.LOST, pageable))
                    .thenReturn(new PageImpl<>(List.of(issue), pageable, 1));

            Page<BookIssueResponseDTO> result = service.getIssuesByStatus(IssueStatus.LOST, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getIssueId()).isEqualTo("BKI000001");
            assertThat(result.getContent().get(0).getBookTitle()).isEqualTo("Clean Code");
        }

        @Test
        void overdueFilterReturnsBooksStillOutAndPastDue() {
            Pageable pageable = PageRequest.of(0, 10);
            BookIssue late = openIssue();
            late.setDueDate(LocalDate.now().minusDays(3));
            when(bookIssueRepository.findOverdueIssues(any(LocalDate.class), eq(pageable)))
                    .thenReturn(new PageImpl<>(List.of(late), pageable, 1));

            Page<BookIssueResponseDTO> result = service.getIssuesByStatus(IssueStatus.OVERDUE, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(IssueStatus.OVERDUE);
            assertThat(result.getContent().get(0).getDaysOverdue()).isEqualTo(3);
        }

        @Test
        void issuedFilterExcludesBooksThatArePastDue() {
            Pageable pageable = PageRequest.of(0, 10);
            when(bookIssueRepository.findCurrentIssues(any(LocalDate.class), eq(pageable)))
                    .thenReturn(Page.empty(pageable));

            service.getIssuesByStatus(IssueStatus.ISSUED, pageable);

            verify(bookIssueRepository).findCurrentIssues(any(LocalDate.class), eq(pageable));
            verify(bookIssueRepository, never()).findByStatus(any(), any());
        }

        @Test
        void returnedFilterUsesTheReturnedQuery() {
            Pageable pageable = PageRequest.of(0, 10);
            when(bookIssueRepository.findReturnedIssues(pageable)).thenReturn(Page.empty(pageable));

            service.getIssuesByStatus(IssueStatus.RETURNED, pageable);

            verify(bookIssueRepository).findReturnedIssues(pageable);
        }

        @Test
        void issuesOfAnUnknownMemberAreNotFound() {
            when(memberRepository.existsById("MEM999999")).thenReturn(false);

            assertThatThrownBy(() -> service.getIssuesByMember("MEM999999", PageRequest.of(0, 10)))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Test
    void issueSavesTheEditionAndTheIssueOnce() {
        service.issueBook(issueRequest(), "ADM000001");

        ArgumentCaptor<BookIssue> captor = ArgumentCaptor.forClass(BookIssue.class);
        verify(bookIssueRepository).save(captor.capture());
        verify(bookEditionRepository).save(edition);
        assertThat(captor.getValue().getReferenceNumber()).startsWith("BKI-");
    }
}
