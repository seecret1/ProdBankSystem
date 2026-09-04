package com.github.seecret1.jwt_common.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class UserPrincipal extends User {

    private final String userId;

    private final String email;

    private final String role;

    public UserPrincipal(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            String userId,
            String email,
            String role
    ) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}