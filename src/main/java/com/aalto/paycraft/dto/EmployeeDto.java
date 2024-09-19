package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class EmployeeDto {

    private UUID employeeId;
    private UUID companyId;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private Date dateOfBirth;
    private String streetAddress;
    private String phoneNumber;
    private String jobTitle;
    private String department;
    private String bankName;
    private String accountNumber;
    private BigDecimal salaryAmount;
    private Currency salaryCurrency;

}
