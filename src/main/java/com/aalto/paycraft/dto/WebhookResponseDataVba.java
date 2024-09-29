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
    private VirtualBankAccountDetailsDTO virtual_bank_account_details;
    private LocalDateTime transaction_date;

    @Data
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VirtualBankAccountDetailsDTO {
        private PayerBankAccountDTO payer_bank_account;
        private VirtualBankAccountDTO virtual_bank_account;
    }

    @Data
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PayerBankAccountDTO {
        private String account_name;
        private String account_number;  // Masked account number
        private String bank_name;
    }

    @Data
    @JsonIgnoreProperties
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VirtualBankAccountDTO {
        private String account_name;
        private String account_number;
        private String account_reference;
        private String bank_name;
    }
}
