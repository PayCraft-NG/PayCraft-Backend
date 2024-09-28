package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualAccountDTO {

    @Schema(description = "Unique identifier for the virtual account", example = "VA123456789")
    private String virtualAccountId;

    @Schema(description = "Virtual account number", example = "1234567890")
    private String accountNumber;

    @Schema(description = "Current balance of the virtual account", example = "5000.00")
    private BigDecimal balance;

    @Schema(description = "Bank code associated with the virtual account", example = "001")
    private String bankCode;

    @Schema(description = "Name of the bank associated with the virtual account", example = "Aalto Bank")
    private String bankName;

    @Schema(description = "Status of the virtual account", example = "Active")
    private String accountStatus;

    @Schema(description = "Currency of the virtual account", example = "USD")
    private Currency currency;

    @Schema(description = "Employer ID associated with the virtual account", example = "EMP123456")
    private String employerId;
}

