package com.library.model.enums;

public enum IssueStatus {
    ISSUED,      // Currently borrowed
    RETURNED,    // Returned on time
    OVERDUE,     // Not yet returned after the due date
    LOST         // Book lost
}