package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutData {
    private BigDecimal amount;
    private String bankCode;
    private String accountNumber;
    private String currency;
    private String email;
    private String fullName;
}
