package com.library.repository;

import com.library.model.BookIssue;
import com.library.model.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookIssueRepository extends JpaRepository<BookIssue, String> {

    @Query("SELECT bi.issueId FROM BookIssue bi ORDER BY bi.issueId DESC LIMIT 1")
    String findLastIssueId();

    Optional<BookIssue> findByReferenceNumber(String referenceNumber);

    Page<BookIssue> findByMember_MemberIdAndStatus(String memberId, IssueStatus status, Pageable pageable);

    List<BookIssue> findByBookEdition_EditionIdAndStatus(String editionId, IssueStatus status);

    @Query("SELECT bi FROM BookIssue bi WHERE bi.member.memberId = :memberId")
    Page<BookIssue> findByMember_MemberId(@Param("memberId") String memberId, Pageable pageable);

    @Query("SELECT COUNT(bi) FROM BookIssue bi WHERE bi.member.memberId = :memberId AND bi.status = 'ISSUED'")
    long countActiveIssuesByMember(@Param("memberId") String memberId);

    @Query("SELECT bi FROM BookIssue bi WHERE bi.dueDate < :currentDate AND bi.status = 'ISSUED'")
    Page<BookIssue> findOverdueIssues(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    Page<BookIssue> findByStatus(IssueStatus status, Pageable pageable);

    boolean existsByBookEdition_EditionIdAndStatus(String editionId, IssueStatus status);
}