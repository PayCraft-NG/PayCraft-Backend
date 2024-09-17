package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enumeration.Currency;
import com.aalto.paycraft.dto.enumeration.CompanySize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyUpdateDTO {

    private String companyName;

    private CompanySize companySize;

    @Email(message = "Company email address format is invalid")
    private String companyEmailAddress;

    @Pattern(regexp = "(^$|[0-9]{13})", message = "Company phone number must be 13 digits")
    private String companyPhoneNumber;

    private String companyStreetAddress;

    private String companyCountry;

    private Currency companyCurrency;

    private UUID companyId;

    private EmployerDTO employerDTO;
}
