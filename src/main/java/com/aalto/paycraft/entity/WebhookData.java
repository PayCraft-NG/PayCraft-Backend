package com.aalto.paycraft.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity @Getter @Setter
@Builder @NoArgsConstructor @AllArgsConstructor
public class WebhookData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Primary key for the entity

    @Column(nullable = false)
    private String event; // The event type (e.g., "charge.success")

    @Column(nullable = false)
    private String reference;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private String status;
}
