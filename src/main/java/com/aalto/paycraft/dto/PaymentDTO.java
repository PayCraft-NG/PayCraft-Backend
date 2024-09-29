package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDTO {

    @Schema(description = "Reference number for the transaction", example = "REF123456789")
    private String referenceNumber;

    @Schema(description = "Amount involved in the transaction", example = "1000.50")
    private BigDecimal amount;

    @Schema(description = "Type of transaction, either CREDIT or DEBIT", example = "CREDIT")
    private String transactionType;

    @Schema(description = "Date and time when the transaction occurred", example = "2023-09-15T14:30:00")
    private LocalDateTime transactionDateTime;

    @Schema(description = "Currency in which the transaction was made", example = "NGN")
    private String currency;

    @Schema(description = "Description of the transaction", example = "Salary payment")
    private String description;

    @Schema(description = "Optional payroll ID if this transaction is part of payroll processing", example = "fksnnvso-alcnaecondc-dajacnjd")
    private String payrollName;

    @Schema(description = "Optional employee name if applicable", example = "John Doe")
    private String employeeName;
}
