package com.aalto.paycraft.dto;

import com.aalto.paycraft.dto.enums.Currency;
import com.aalto.paycraft.dto.enums.CompanySize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "CompanyDTO",
        description = "Schema to hold Company Information"
)
public class CompanyDTO {

    @Schema(
            description = "Name of the company",
            example = "Aalto Technologies"
    )
    @NotEmpty(message = "Company name cannot be null or empty")
    private String companyName;

    @Schema(
            description = "Size of the company",
            example = "MEDIUM",
            allowableValues = {"SMALL", "MEDIUM", "LARGE", "ENTERPRISE"}
    )
    @NotNull(message = "Company size cannot be null or empty")
    private CompanySize companySize;

    @Schema(
            description = "Email address of the company",
            example = "info@aalto.com"
    )
    @Email(message = "Company email address format is invalid")
    @NotEmpty(message = "Company email address cannot be null or empty")
    private String companyEmailAddress;

    @Schema(
            description = "Phone number of the company (13 digits)",
            example = "2341234567890"
    )
    @NotEmpty(message = "Company phone number cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Company phone number must be 13 digits")
    private String companyPhoneNumber;

    @Schema(
            description = "Street address of the company",
            example = "1234 Tech Park, Lagos"
    )
    @NotEmpty(message = "Company street address cannot be null or empty")
    private String companyStreetAddress;

    @Schema(
            description = "Country where the company is located",
            example = "Nigeria"
    )
    @NotEmpty(message = "Company country cannot be null or empty")
    private String companyCountry;

    @Schema(
            description = "Currency used by the company",
            example = "NGN",
            allowableValues = {"NGN", "USD"}
    )
    @NotNull(message = "Company currency cannot be null")
    private Currency companyCurrency;

    @Schema(
            description = "Unique identifier of the company",
            example = "d290f1ee-6c54-4b01-90e6-d701748f0851"
    )
    private UUID companyId;

    @Schema(
            description = "Employer information associated with the company"
    )
    private EmployerDTO employerDTO;
}
