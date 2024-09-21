package com.aalto.paycraft.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity @Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = {"employer"})
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String accessToken;

    @Column(nullable = false, unique = true, length = 2048)
    private String refreshToken;

    @Builder.Default
    @Column(nullable = false)
    private Boolean expired = false;

    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(updatable = false)  // This ensures the createdAt field is only set once during creation
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employerId", referencedColumnName = "employerId")
    private Employer employer;
}
