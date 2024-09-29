package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CardFundingRequestDTO {
    private String cardNumber;
    private String cvv;
    private String expiryMonth;
    private String expiryYear;
    private String cardPin;
    private BigDecimal amount;
}
