package com.aalto.paycraft.dto;


import com.aalto.paycraft.dto.enums.PayrollFrequency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayrollUpdateDTO {
    private Boolean automatic; // Indicates if the payroll is automatic or manual
    private PayrollFrequency frequency; // Optional, frequency of payroll execution (nullable)
}
