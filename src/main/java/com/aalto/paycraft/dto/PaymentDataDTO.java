package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDataDTO {
    private BigDecimal amount;
    private BigDecimal amount_charged;
    private String auth_model;
    private String currency;
    private BigDecimal fee;
    private BigDecimal vat;
    private String response_message;
    private String payment_reference;
    private String status;
    private String transaction_reference;
    private CardDTO card;
}


