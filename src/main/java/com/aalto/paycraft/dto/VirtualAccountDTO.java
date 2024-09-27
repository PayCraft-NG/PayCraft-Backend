package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VirtualAccountDTO {
    private String virtualAccountId;
    private String accountNumber;
    private BigDecimal balance;
    private String bankCode;
    private String bankName;
    private String accountStatus;
    private Currency currency;
    private String employerId;
}
