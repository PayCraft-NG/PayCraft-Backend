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
    private String event; // The event type (e.g., "charge.success")
    private String reference;
    private String currency;
    private BigDecimal amount;
    private BigDecimal fee;
    private String status;
}
