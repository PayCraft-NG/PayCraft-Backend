package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualAccountTransactionDTO {
    private int totalPages;
    private List<TransactionDTO> transactions;

    @Data @Builder
    public static class TransactionDTO {
        private String payerAccountNumber;
        private String payerAccountName;
        private String payerBankName;
        private String reference;
        private String status;
        private String amount;
        private String fee;
        private String currency;
        private String description;
    }
}
