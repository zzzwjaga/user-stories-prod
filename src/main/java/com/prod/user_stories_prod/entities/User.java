package com.prod.user_stories_prod.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public record User(UUID id, String username, String email, String password_hash, Timestamp created_at,
                   Timestamp updated_at) implements UserDetails {

    public User(UUID id, String username, String email, String password_hash) {
        this(id,username,email,password_hash, null, null);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }
}
