package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankTransferDetailsDTO {
    private BigDecimal amount;
    private BigDecimal amountExpected;
    private String referenceNumber;
    private String paymentReference;
    private String accountName;
    private String accountNumber;
    private String bankName;
    private String expiryDate;
}
