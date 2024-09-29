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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "EmployeeRequestDTO",
        description = "Schema to hold Employee request information for creating or updating an employee"
)
public class EmployeeRequestDto {

    @Schema(
            description = "First name of the employee",
            example = "John"
    )
    @Size(min = 3, max = 100)
    @NotEmpty(message = "First name cannot be null or empty")
    private String firstName;

    @Schema(
            description = "Last name of the employee",
            example = "Doe"
    )
    @Size(min = 3, max = 100)
    @NotEmpty(message = "Last name cannot be null or empty")
    private String lastName;

    @Schema(
            description = "Email address of the employee",
            example = "john.doe@aalto.com"
    )
    @Email(message = "Email address format is invalid")
    @NotEmpty(message = "Email address cannot be null or empty")
    private String emailAddress;

    @Schema(
            description = "Date of birth of the employee",
            example = "1990-01-15"
    )
    @NotNull(message = "Date of Birth cannot be null or empty")
    private Date dateOfBirth;

    @Schema(
            description = "Street address of the employee",
            example = "4567 Code Ave, Lagos"
    )
    @NotEmpty(message = "Street address cannot be null or empty")
    private String streetAddress;

    @Schema(
            description = "Phone number of the employee (13 digits)",
            example = "2349876543210"
    )
    @NotEmpty(message = "Phone number cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Phone number must be 13 digits")
    private String phoneNumber;

    @Schema(
            description = "Job title of the employee",
            example = "Software Engineer"
    )
    @Size(min = 3, max = 100)
    @NotEmpty(message = "Job title cannot be null or empty")
    private String jobTitle;

    @Schema(
            description = "Department where the employee works",
            example = "Engineering"
    )
    @Size(min = 3, max = 100)
    @NotEmpty(message = "Department cannot be null or empty")
    private String department;

    @Schema(
            description = "Bank Verification Number (BVN) of the employee",
            example = "12345678901"
    )
    @NotEmpty(message = "BVN cannot be null or empty")
    private String bvn;

    @Schema(
            description = "Bank name where the employee's salary is paid",
            example = "Zenith Bank"
    )
    @NotEmpty(message = "Bank Name cannot be null or empty")
    private String bankName;

    @Schema(
            description = "Bank account number of the employee",
            example = "1234567890"
    )
    @Pattern(regexp = "(^$|[0-9]{10})", message = "Bank account number must be 10 digits")
    @NotEmpty(message = "Bank account number cannot be null or empty")
    private String accountNumber;

    @Schema(
            description = "Salary amount of the employee",
            example = "500000.00"
    )
    @NotNull(message = "Amount of Salary cannot be null or empty")
    private BigDecimal salaryAmount;

    @Schema(
            description = "Currency in which the employee's salary is paid",
            example = "NGN",
            allowableValues = {"NGN", "USD"}
    )
    @NotNull(message = "Currency of Salary cannot be null or empty")
    private Currency salaryCurrency;
}
