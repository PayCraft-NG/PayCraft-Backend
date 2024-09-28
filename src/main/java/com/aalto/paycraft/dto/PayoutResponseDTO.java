package com.aalto.paycraft.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayoutResponseDTO {
    private String amount;
    private String fee;
    private String currency;
    private String status;
    private String reference;
    private String narration;
    private CustomerDTO customer;
}
