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
    private BigDecimal total_chargeable_amount;
    private boolean merchant_bears_cost;
    private String currency;
    private String reference;
    private String description;
    private LocalDateTime created_at;
}
