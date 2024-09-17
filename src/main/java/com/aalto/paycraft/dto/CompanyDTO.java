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
public class CompanyDTO {

    @NotEmpty(message = "Company name cannot be null or empty")
    private String companyName;

    @NotNull(message = "Company size cannot be null or empty")
    private CompanySize companySize;

    @Email(message = "Company email address format is invalid")
    @NotEmpty(message = "Company email address cannot be null or empty")
    private String companyEmailAddress;

    @NotEmpty(message = "Company phone number cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Company phone number must be 13 digits")
    private String companyPhoneNumber;

    @NotEmpty(message = "Company street address cannot be null or empty")
    private String companyStreetAddress;

    @NotEmpty(message = "Company country cannot be null or empty")
    private String companyCountry;

    @NotNull(message = "Company currency cannot be null")
    private Currency companyCurrency;

    private UUID companyId;

    private EmployerDTO employerDTO;
}
