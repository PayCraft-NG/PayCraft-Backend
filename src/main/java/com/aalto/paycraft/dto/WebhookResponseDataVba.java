package com.aalto.paycraft.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookResponseDataVba {

    private String reference;
    private String currency;
    private BigDecimal amount;
    private BigDecimal fee;
    private String status;
    private VirtualBankAccountDetailsDTO virtualBankAccountDetails;
    private LocalDateTime transactionDate;

    @Data
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VirtualBankAccountDetailsDTO {
        private PayerBankAccountDTO payerBankAccount;
        private VirtualBankAccountDTO virtualBankAccount;
    }

    @Data
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PayerBankAccountDTO {
        private String accountName;
        private String accountNumber;  // Masked account number
        private String bankName;
    }

    @Data
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VirtualBankAccountDTO {
        private String accountName;
        private String accountNumber;
        private String accountReference;
        private String bankName;
    }
}
