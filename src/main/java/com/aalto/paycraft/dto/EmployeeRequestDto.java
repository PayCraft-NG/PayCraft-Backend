package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor @JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeRequestDto {
    @Size(min = 3, max = 100)
    @NotEmpty(message = "First name cannot be null or empty")
    private String firstName;

    @Size(min = 3, max = 100)
    @NotEmpty(message = "Last name cannot be null or empty")
    private String lastName;

    @Email(message = "Email address format is invalid")
    @NotEmpty(message = "Email address cannot be null or empty")
    private String emailAddress;

    @NotNull(message = "Date of Birth cannot be null or empty")
    private Date dateOfBirth;

    @NotEmpty(message = "Street address cannot be null or empty")
    private String streetAddress;

    @NotEmpty(message = "Phone number cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Phone number must be 13 digits")
    private String phoneNumber;

    @Size(min = 3, max = 100)
    @NotEmpty(message = "Job title cannot be null or empty")
    private String jobTitle;

    @Size(min = 3, max = 100)
    @NotEmpty(message = "Department cannot be null or empty")
    private String department;

    @NotEmpty(message = "BVN cannot be null or empty")
    private String bvn;

    @NotEmpty(message = "Bank Name cannot be null or empty")
    private String bankName;

    @Pattern(regexp = "(^$|[0-9]{10})", message = "Bank account number must be 10 digits")
    @NotEmpty(message = "Bank account number cannot be null or empty")
    private String accountNumber;

    @NotNull(message = "Amount of Salary cannot be null or empty")
    private BigDecimal salaryAmount;

    @NotNull(message = "Currency of Salary cannot be null or empty")
    private Currency salaryCurrency;
}
