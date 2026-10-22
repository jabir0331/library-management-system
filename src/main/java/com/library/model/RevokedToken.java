package com.library.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** A JWT that was logged out before its natural expiry, identified by its jti claim. */
@Entity
@Table(name = "revoked_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @Column(name = "jti", length = 64)
    private String jti;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
}
