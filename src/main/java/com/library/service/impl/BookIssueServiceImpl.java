package com.library.service.impl;

import com.library.dto.request.BookIssueRequestDTO;
import com.library.dto.request.BookReturnRequestDTO;
import com.library.dto.response.BookIssueResponseDTO;
import com.library.exception.ResourceNotFoundException;
import com.library.model.*;
import com.library.model.enums.IssueStatus;
import com.library.model.enums.ReturnCondition;
import com.library.repository.*;
import com.library.service.BookIssueService;
import com.library.service.SystemConfigService;
import com.library.util.BookIssueIdGenerator;
import com.library.util.BookIssueReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookIssueServiceImpl implements BookIssueService {

    private final BookIssueRepository bookIssueRepository;
    private final MemberRepository memberRepository;
    private final BookEditionRepository bookEditionRepository;
    private final AdminRepository adminRepository;
    private final SystemConfigService configService;
    private final BookIssueIdGenerator issueIdGenerator;
    private final BookIssueReferenceGenerator referenceGenerator;

    @Override
    public BookIssueResponseDTO issueBook(BookIssueRequestDTO request) {
        log.info("Processing book issue request for member: {}", request.getMemberId());

        // 1. Validate Member
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member", request.getMemberId()));

        if (member.getStatus() != Member.MemberStatus.ACTIVE) {
            throw new RuntimeException("Member is not active. Current status: " + member.getStatus());
        }

        // 2. Validate Book Edition
        BookEdition edition = bookEditionRepository.findById(request.getBookEditionId())
                .orElseThrow(() -> new ResourceNotFoundException("BookEdition", request.getBookEditionId()));

        if (edition.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for this edition");
        }

        // 3. Check if member has reached max books limit
        int maxBooks = configService.getMaxBooksPerMember();
        long activeIssues = bookIssueRepository.countActiveIssuesByMember(member.getMemberId());
        if (activeIssues >= maxBooks) {
            throw new RuntimeException("Member has reached maximum book limit: " + maxBooks);
        }

        // 4. Validate Admin
        Admin issuedBy = adminRepository.findById(request.getIssuedById())
                .orElseThrow(() -> new ResourceNotFoundException("Admin", request.getIssuedById()));

        // 5. Set issue date
        LocalDate issuedDate = request.getIssuedDate() != null ? request.getIssuedDate() : LocalDate.now();

        // 6. Calculate due date - use provided or calculate from config
        LocalDate dueDate = request.getDueDate();
        if (dueDate == null) {
            int maxIssueDays = configService.getMaxIssueDays();
            dueDate = issuedDate.plusDays(maxIssueDays);
        }

        // 7. Validate due date is after issued date
        if (dueDate.isBefore(issuedDate) || dueDate.isEqual(issuedDate)) {
            throw new RuntimeException("Due date must be after issued date");
        }

        // 8. Create BookIssue
        BookIssue issue = new BookIssue();
        String lastIssueId = bookIssueRepository.findLastIssueId();
        issue.setIssueId(issueIdGenerator.generateNextId(lastIssueId));
        issue.setReferenceNumber(referenceGenerator.generateReferenceNumber());
        issue.setMember(member);
        issue.setBookEdition(edition);
        issue.setIssuedBy(issuedBy);
        issue.setIssuedDate(issuedDate);
        issue.setDueDate(dueDate);
        issue.setStatus(IssueStatus.ISSUED);

        // 9. Decrease available copies
        edition.decrementAvailableCopies();
        bookEditionRepository.save(edition);

        // 10. Save issue
        BookIssue savedIssue = bookIssueRepository.save(issue);
        log.info("Book issued successfully: {}", savedIssue.getReferenceNumber());

        return mapToResponseDTO(savedIssue);
    }

    @Override
    public BookIssueResponseDTO returnBook(BookReturnRequestDTO request) {
        log.info("Processing book return for issue: {}", request.getIssueId());

        // 1. Find issue record
        BookIssue issue = bookIssueRepository.findById(request.getIssueId())
                .orElseThrow(() -> new ResourceNotFoundException("BookIssue", request.getIssueId()));

        if (issue.isReturned()) {
            throw new RuntimeException("Book already returned on: " + issue.getReturnedDate());
        }

        if (issue.isLost()) {
            throw new RuntimeException("Book is marked as lost. Cannot return.");
        }

        // 2. Validate return date
        LocalDate returnDate = request.getReturnedDate() != null ?
                request.getReturnedDate() : LocalDate.now();

        if (returnDate.isBefore(issue.getIssuedDate())) {
            throw new RuntimeException("Return date cannot be before issue date");
        }

        // 3. Validate Admin
        Admin receivedBy = adminRepository.findById(request.getReceivedById())
                .orElseThrow(() -> new ResourceNotFoundException("Admin", request.getReceivedById()));

        // 4. Calculate fine if overdue
        BigDecimal fineAmount = calculateFine(issue, returnDate);

        // 5. Determine status
        IssueStatus status = returnDate.isAfter(issue.getDueDate()) ?
                IssueStatus.OVERDUE : IssueStatus.RETURNED;

        // 6. Update issue record
        issue.setReturnedDate(returnDate);
        issue.setReceivedBy(receivedBy);
        issue.setReturnCondition(request.getReturnCondition() != null ?
                request.getReturnCondition() : ReturnCondition.GOOD);
        issue.setStatus(status);
        issue.setFineAmount(fineAmount);

        // 7. Increase available copies
        BookEdition edition = issue.getBookEdition();
        edition.incrementAvailableCopies();
        bookEditionRepository.save(edition);

        // 8. Save issue
        BookIssue savedIssue = bookIssueRepository.save(issue);
        log.info("Book returned successfully: {}", savedIssue.getReferenceNumber());

        return mapToResponseDTO(savedIssue);
    }

    @Override
    public BookIssueResponseDTO markBookAsLost(String issueId, String adminId, BigDecimal penaltyAmount) {
        log.info("Marking book as lost for issue: {}", issueId);

        BookIssue issue = bookIssueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("BookIssue", issueId));

        if (issue.isReturned()) {
            throw new RuntimeException("Cannot mark as lost. Book already returned.");
        }

        if (issue.isLost()) {
            throw new RuntimeException("Book already marked as lost.");
        }

        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));

        // Calculate penalty if not provided
        BigDecimal finalPenalty = penaltyAmount != null ?
                penaltyAmount : calculateLostBookPenalty(issue);

        // Update issue
        issue.setStatus(IssueStatus.LOST);
        issue.setLostBookPenalty(finalPenalty);
        issue.setReturnedDate(LocalDate.now());
        issue.setReceivedBy(admin);
        issue.setReturnCondition(ReturnCondition.LOST);

        // Decrease total copies (book is permanently lost)
        BookEdition edition = issue.getBookEdition();
        edition.setTotalCopies(edition.getTotalCopies() - 1);
        if (edition.getAvailableCopies() > 0) {
            edition.setAvailableCopies(edition.getAvailableCopies() - 1);
        }
        bookEditionRepository.save(edition);

        BookIssue savedIssue = bookIssueRepository.save(issue);
        log.info("Book marked as lost: {}", savedIssue.getReferenceNumber());

        return mapToResponseDTO(savedIssue);
    }

    @Override
    public BookIssueResponseDTO getIssueById(String issueId) {
        BookIssue issue = bookIssueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("BookIssue", issueId));
        return mapToResponseDTO(issue);
    }

    @Override
    public BookIssueResponseDTO getIssueByReferenceNumber(String referenceNumber) {
        BookIssue issue = bookIssueRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("BookIssue not found with reference: " + referenceNumber));
        return mapToResponseDTO(issue);
    }

    @Override
    public List<BookIssueResponseDTO> getAllIssues() {
        return bookIssueRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookIssueResponseDTO> getIssuesByMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return bookIssueRepository.findByMember_MemberId(memberId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookIssueResponseDTO> getActiveIssuesByMember(String memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("Member", memberId);
        }
        return bookIssueRepository.findByMember_MemberIdAndStatus(memberId, IssueStatus.ISSUED)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookIssueResponseDTO> getOverdueIssues() {
        return bookIssueRepository.findOverdueIssues(LocalDate.now())
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookIssueResponseDTO> getIssuesByStatus(IssueStatus status) {
        // ... implementation
        return null; // Placeholder
    }

    @Override
    public boolean canIssueBook(String memberId, String bookEditionId) {
        // Check member status
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member", memberId));

        if (member.getStatus() != Member.MemberStatus.ACTIVE) {
            return false;
        }

        // Check active issues count
        int maxBooks = configService.getMaxBooksPerMember();
        long activeIssues = bookIssueRepository.countActiveIssuesByMember(memberId);
        if (activeIssues >= maxBooks) {
            return false;
        }

        // Check available copies
        BookEdition edition = bookEditionRepository.findById(bookEditionId)
                .orElseThrow(() -> new ResourceNotFoundException("BookEdition", bookEditionId));

        return edition.getAvailableCopies() > 0;
    }

    @Override
    public BigDecimal calculateFine(BookIssue issue, LocalDate returnDate) {
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        // If returned on or before due date, no fine
        if (!returnDate.isAfter(issue.getDueDate())) {
            return BigDecimal.ZERO;
        }

        // Calculate days overdue
        long daysOverdue = ChronoUnit.DAYS.between(issue.getDueDate(), returnDate);

        // Apply grace period
        int gracePeriod = configService.getGracePeriodDays();
        long daysAfterGrace = daysOverdue - gracePeriod;
        if (daysAfterGrace <= 0) {
            return BigDecimal.ZERO;
        }

        // Calculate fine
        BigDecimal finePerDay = configService.getFinePerDay();
        BigDecimal fine = finePerDay.multiply(BigDecimal.valueOf(daysAfterGrace));
        return fine.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateLostBookPenalty(BookIssue issue) {
        BigDecimal bookPrice = issue.getBookEdition().getPrice();
        BigDecimal multiplier = configService.getLostBookMultiplier();
        BigDecimal serviceCharge = configService.getServiceCharge();

        BigDecimal penalty = bookPrice.multiply(multiplier).add(serviceCharge);
        return penalty.setScale(2, RoundingMode.HALF_UP);
    }

    // ═══════════════════════════════════════════════
    // PRIVATE HELPERS
    // ═══════════════════════════════════════════════

    private BookIssueResponseDTO mapToResponseDTO(BookIssue issue) {
        BookIssueResponseDTO response = new BookIssueResponseDTO();

        // Basic info
        response.setIssueId(issue.getIssueId());
        response.setReferenceNumber(issue.getReferenceNumber());
        response.setIssuedDate(issue.getIssuedDate());
        response.setDueDate(issue.getDueDate());
        response.setStatus(issue.getStatus());

        // Member info
        response.setMemberId(issue.getMember().getMemberId());
        response.setMemberName(issue.getMember().getFullName());

        // Book Edition info
        BookEdition edition = issue.getBookEdition();
        response.setBookEditionId(edition.getEditionId());
        response.setBookTitle(edition.getBook().getTitle());
        response.setIsbn(edition.getIsbn());

        // Admin info
        response.setIssuedBy(issue.getIssuedBy().getUsername());

        // Return info
        if (issue.isReturned()) {
            response.setReturnedDate(issue.getReturnedDate());
            response.setReturnCondition(issue.getReturnCondition());
            if (issue.getReceivedBy() != null) {
                response.setReceivedBy(issue.getReceivedBy().getUsername());
            }
        }

        // Financial
        response.setFineAmount(issue.getFineAmount());
        response.setFinePaid(issue.isFinePaid());
        response.setLostBookPenalty(issue.getLostBookPenalty());

        // Calculated fields
        response.setOverdue(issue.isOverdue());
        response.setDaysOverdue(issue.getDaysOverdue());

        // Audit
        response.setCreatedAt(issue.getCreatedAt());
        response.setUpdatedAt(issue.getUpdatedAt());

        return response;
    }
}