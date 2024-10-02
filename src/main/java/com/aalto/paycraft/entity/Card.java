package com.aalto.paycraft.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;
import java.util.UUID;

@Builder @Entity @Getter
@Setter @ToString
@AllArgsConstructor @NoArgsConstructor
@Table(name = "cards")
public class Card {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String expiryMonth;

    @Column(nullable = false)
    private String expiryYear;

    @Column(nullable = false)
    private String cvv;

    @Column(nullable = false)
    private String cardPin;

    @ManyToOne(fetch = FetchType.LAZY)
    private VirtualAccount account;
}
