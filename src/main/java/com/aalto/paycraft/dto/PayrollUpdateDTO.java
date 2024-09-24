package com.aalto.paycraft.dto;


import com.aalto.paycraft.dto.enums.PayrollFrequency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Indicates if the payroll is automatic or manual", example = "true")
    private Boolean automatic; // Indicates if the payroll is automatic or manual

    @Schema(description = "Exact period in cron expression to run payroll", example = "* * * * * 5")
    private String cronExpression;
}
