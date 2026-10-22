package com.library.security;

import lombok.Getter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/** Authenticated admin; carries the admin ID so controllers can use it without another lookup. */
@Getter
public class AdminPrincipal extends User {

    private final String adminId;

    public AdminPrincipal(String adminId, String username, String password) {
        super(username, password, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        this.adminId = adminId;
    }
}
