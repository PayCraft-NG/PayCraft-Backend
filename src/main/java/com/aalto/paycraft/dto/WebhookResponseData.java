package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookResponseData {
    private String reference;
    private String payment_reference;
    private String currency;
    private BigDecimal amount;
    private BigDecimal fee;
    private String payment_method;
    private String status;
}
