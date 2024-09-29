package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.PaymentStatus;
import com.aalto.paycraft.dto.enums.PayrollFrequency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "DTO representing payroll data")
public class PayrollDTO {

    @Schema(description = "Unique identifier for the payroll", example = "b9b5f6a2-ef72-43d2-8b4d-4e2ecb0f0278")
    private UUID payrollId;

    @NotNull(message = "Name of PayRoll is required")
    @Schema(description = "Name for the payroll", example = "FrontDesk-J")
    private String payrollName;

    @NotNull(message = "Automatic flag is required")
    @Schema(description = "Indicates if the payroll is automatic or manual", example = "true")
    private Boolean automatic;

    @DecimalMin(value = "0.0", inclusive = false, message = "Total salary must be greater than 0")
    @Schema(description = "Total salary of the payroll", example = "50000.00")
    private BigDecimal totalSalary;

    @Schema(description = "Start of the pay period", example = "2023-09-01")
    private LocalDate payPeriodStart;

    @Schema(description = "End of the pay period", example = "2023-09-30")
    private LocalDate payPeriodEnd;

    @Schema(description = "Last time the payroll ran", example = "2023-09-30")
    private LocalDate lastRunDate;

    @Schema(description = "Exact period in cron expression to run payroll", example = "*/5 * * * * *")
    private String cronExpression;

    @Schema(description = "Status of the payroll", example = "PENDING")
    private PaymentStatus paymentStatus;

    @Schema(description = "Associated company data")
    private CompanyDTO companyDTO;

    @Schema(description = "List of employee IDs associated with the payroll", example = "[\"9e57a4f2-00c3-4b9b-b3fe-b3a8c1e7bc88\"]")
    private List<UUID> employees;
}
