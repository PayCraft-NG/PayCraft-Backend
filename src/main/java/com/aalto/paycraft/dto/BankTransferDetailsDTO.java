package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankTransferDetailsDTO {

    @Schema(description = "Amount transferred", example = "1500.75")
    private BigDecimal amount;

    @Schema(description = "Expected amount for the transfer", example = "1500.75")
    private BigDecimal amountExpected;

    @Schema(description = "Reference number of the bank transfer", example = "REF123456789")
    private String referenceNumber;

    @Schema(description = "Payment reference for the transfer", example = "PAY123456789")
    private String paymentReference;

    @Schema(description = "Account name for the transfer", example = "John Doe")
    private String accountName;

    @Schema(description = "Account number for the transfer", example = "1234567890")
    private String accountNumber;

    @Schema(description = "Name of the bank where the account is held", example = "Aalto Bank")
    private String bankName;

    @Schema(description = "Expiry date for the transfer", example = "2023-12-31")
    private String expiryDate;
}

