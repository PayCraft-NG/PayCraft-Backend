package com.aalto.paycraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder @Entity
@Getter @Setter @ToString
@AllArgsConstructor @NoArgsConstructor
@Table(name = "payments")
public class Payment {

    @Id @GeneratedValue
    @JdbcTypeCode(Types.VARCHAR)
    private UUID paymentId;

    @Column(nullable = false)
    private String referenceNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String transactionType;  // E.g., "CREDIT" or "DEBIT"

    @Column(nullable = false)
    private LocalDateTime transactionDateTime;

    // Optional: Other metadata
    @Column(nullable = true)
    private String description;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = true)
    private String payrollName;

    @Column(nullable = true)
    private String employeeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accountId", nullable = false)
    private VirtualAccount account;
}
