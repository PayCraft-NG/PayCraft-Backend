package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankAccountDTO {
    private String account_name;
    private String account_number;
    private String bank_name;
    private String bank_code;
    private LocalDateTime expiry_date_in_utc;
}
