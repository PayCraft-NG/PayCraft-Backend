package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyPaymentDTO {
    private String reference;
    private String status;
    private double amount;
    private double amountPaid;
    private double fee;
    private String currency;
    private String description;
    private CardDTO card;
}
