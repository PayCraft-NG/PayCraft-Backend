package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualAccountTransactionDTO {

    @Schema(description = "Total number of pages in the transaction list", example = "5")
    private int totalPages;

    @Schema(description = "List of transactions related to the virtual account")
    private List<TransactionDTO> transactions;

    @Data @Builder
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TransactionDTO {

        @Schema(description = "Payer's account number", example = "9876543210")
        private String payerAccountNumber;

        @Schema(description = "Payer's account name", example = "Jane Smith")
        private String payerAccountName;

        @Schema(description = "Payer's bank name", example = "Aalto Bank")
        private String payerBankName;

        @Schema(description = "Reference for the transaction", example = "TRX123456789")
        private String reference;

        @Schema(description = "Status of the transaction", example = "Completed")
        private String status;

        @Schema(description = "Amount of the transaction", example = "150.50")
        private String amount;

        @Schema(description = "Fee charged for the transaction", example = "2.50")
        private String fee;

        @Schema(description = "Currency of the transaction", example = "USD")
        private String currency;

        @Schema(description = "Description of the transaction", example = "Payment for services")
        private String description;
    }
}
