package com.aalto.paycraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Builder @Entity
@Getter @Setter @AllArgsConstructor
@NoArgsConstructor @ToString
@Table(name = "Employer")
public class Employer extends BaseEntity implements UserDetails {

    @Id @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID employerId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String emailAddress;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String streetAddress;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false, unique = true)
    private String bvn;

    private String password;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return emailAddress;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Builder.Default
    @Column(nullable = false)
    private boolean deleted = false;  // soft delete flag

    @Column
    private LocalDateTime deletedAt;

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        this.deletedAt = deleted ? LocalDateTime.now() : null;  // Set timestamp when deleted
    }
}
