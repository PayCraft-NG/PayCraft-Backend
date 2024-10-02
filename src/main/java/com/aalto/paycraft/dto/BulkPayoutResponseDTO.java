package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BulkPayoutResponseDTO {
    private String status;
    private BigDecimal totalChargeableAmount;
    private boolean merchantBearsCost;
    private String currency;
    private String reference;
    private String description;
    private LocalDateTime createdAt;
}
