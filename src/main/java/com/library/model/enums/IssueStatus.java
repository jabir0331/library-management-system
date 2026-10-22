package com.library.model.enums;

public enum IssueStatus {
    ISSUED,      // Borrowed and not yet due
    RETURNED,    // Returned (a late return carries a fine, see BookIssue.fineAmount)
    OVERDUE,     // Derived, never stored: borrowed and past the due date
    LOST         // Book lost
}