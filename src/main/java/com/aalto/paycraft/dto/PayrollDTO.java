package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enumeration.PaymentStatus;
import com.aalto.paycraft.dto.enumeration.PayrollFrequency;
import com.aalto.paycraft.entity.Company;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PayrollDTO {

    private UUID payrollId; // Unique identifier for the payroll

    @NotNull(message = "Automatic flag is required")
    private Boolean automatic; // Indicates if the payroll is automatic or manual

    @DecimalMin(value = "0.0", inclusive = false, message = "Total salary must be greater than 0")
    private BigDecimal totalSalary; // Optional, total salary (nullable)

    private LocalDate payPeriodStart; // Optional, start of the pay period (nullable)

    private LocalDate payPeriodEnd; // Optional, end of the pay period (nullable)

    private LocalDate lastRunDate; // Optional, last time the payroll ran

    private PayrollFrequency frequency; // Optional, frequency of payroll execution (nullable)

    private PaymentStatus paymentStatus; // Required, status of the payroll

    private CompanyDTO companyDTO; // reference to the associated company
}
