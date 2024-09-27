package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VBATransactionDTO {
    private double total_amount_received;
    private String account_number;
    private String currency;
    private List<TransactionDTO> transactions;
    private PaginationDTO pagination;

    @Data
    public static class TransactionDTO {
        private String reference;
        private String status;
        private String amount;
        private String fee;
        private String currency;
        private String description;
        private PayerBankAccountDTO payer_bank_account;
    }

    @Data
    public static class PayerBankAccountDTO {
        private String account_number;
        private String account_name;
        private String bankName;
    }

    @Data
    public static class PaginationDTO {
        private int page;
        private int total;
        private int page_count;
        private int total_pages;
    }
}
