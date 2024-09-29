package com.aalto.paycraft.entity;

import com.aalto.paycraft.dto.enums.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.List;
import java.util.UUID;

@Builder @Entity
@Getter @Setter @ToString
@AllArgsConstructor @NoArgsConstructor
@Table(name = "VirtualAccount")
public class VirtualAccount {

    @Id @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID accountId;

    // Account number (can be fetched from Korapay API response)
    @Column(unique = true, nullable = false)
    private String accountNumber;

    // The balance of the virtual account
    @Column(nullable = false)
    private BigDecimal balance;

    // Account reference generated when creating account
    @Column(unique = true, nullable = false)
    private String accountReference;

    // Account reference returned by Kora when creating account
    @Column(unique = true, nullable = false)
    private String koraAccountReference;

    // Bank code (from the response)
    @Column(nullable = false)
    private String bankCode;

    // Bank Name (from the response)
    @Column(nullable = false)
    private String bankName;

    // Status of the virtual account
    @Column(nullable = false)
    private String accountStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Currency currency;

    @OneToOne
    @JoinColumn(name = "employerId", nullable = false)
    private Employer employer;

    @OneToMany(mappedBy = "virtualAccount", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
}
