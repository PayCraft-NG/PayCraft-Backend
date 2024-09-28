package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankTransferResponseDTO {
    private String currency;
    private double amount;
    private double amount_expected;
    private double fee;
    private double vat;
    private String reference;
    private String payment_reference;
    private String status;
    private String narration;
    private boolean merchant_bears_cost;
    private BankAccountDTO bank_account;
    private CustomerDTO customer;
}
