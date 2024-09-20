package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "EmployeeDTO",
        description = "Schema to hold Employee Information"
)
public class EmployeeDto {

    @Schema(
            description = "Unique identifier of the employee",
            example = "d290f1ee-6c54-4b01-90e6-d701748f0851"
    )
    private UUID employeeId;

    @Schema(
            description = "Unique identifier of the company associated with the employee",
            example = "f84b1fe2-4c78-4f55-9c23-4f4b4e1c24f3"
    )
    private UUID companyId;

    @Schema(
            description = "First name of the employee",
            example = "John"
    )
    @NotEmpty(message = "First name cannot be null or empty")
    private String firstName;

    @Schema(
            description = "Last name of the employee",
            example = "Doe"
    )
    @NotEmpty(message = "Last name cannot be null or empty")
    private String lastName;

    @Schema(
            description = "Email address of the employee",
            example = "john.doe@aalto.com"
    )
    @Email(message = "Employee email address format is invalid")
    @NotEmpty(message = "Email address cannot be null or empty")
    private String emailAddress;

    @Schema(
            description = "Date of birth of the employee",
            example = "1990-01-15"
    )
    @NotNull(message = "Date of birth cannot be null")
    private Date dateOfBirth;

    @Schema(
            description = "Street address of the employee",
            example = "4567 Code Ave, Lagos"
    )
    @NotEmpty(message = "Street address cannot be null or empty")
    private String streetAddress;

    @Schema(
            description = "Phone number of the employee",
            example = "2349876543210"
    )
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Phone number must be 13 digits")
    @NotEmpty(message = "Phone number cannot be null or empty")
    private String phoneNumber;

    @Schema(
            description = "Job title of the employee",
            example = "Software Engineer"
    )
    @NotEmpty(message = "Job title cannot be null or empty")
    private String jobTitle;

    @Schema(
            description = "Department where the employee works",
            example = "Engineering"
    )
    @NotEmpty(message = "Department cannot be null or empty")
    private String department;

    @Schema(
            description = "Bank name where the employee's salary is paid",
            example = "Zenith Bank"
    )
    @NotEmpty(message = "Bank name cannot be null or empty")
    private String bankName;

    @Schema(
            description = "Account number of the employee",
            example = "1234567890"
    )
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Account number must be 10 digits")
    @NotEmpty(message = "Account number cannot be null or empty")
    private String accountNumber;

    @Schema(
            description = "Salary amount of the employee",
            example = "500000.00"
    )
    @NotNull(message = "Salary amount cannot be null")
    private BigDecimal salaryAmount;

    @Schema(
            description = "Currency in which the employee's salary is paid",
            example = "NGN",
            allowableValues = {"NGN", "USD"}
    )
    @NotNull(message = "Salary currency cannot be null")
    private Currency salaryCurrency;
}
