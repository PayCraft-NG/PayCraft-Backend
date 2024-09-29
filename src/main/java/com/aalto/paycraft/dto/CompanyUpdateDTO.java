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
        name = "CompanyUpdateDTO",
        description = "Schema for updating Company Information"
)
public class CompanyUpdateDTO {

    @Schema(
            description = "Updated name of the company",
            example = "Aalto Technologies"
    )
    private String companyName;

    @Schema(
            description = "Updated size of the company",
            example = "MEDIUM",
            allowableValues = {"SMALL", "MEDIUM", "LARGE", "ENTERPRISE"}
    )
    private CompanySize companySize;

    @Schema(
            description = "Updated email address of the company",
            example = "update@aalto.com"
    )
    @Email(message = "Company email address format is invalid")
    private String companyEmailAddress;

    @Schema(
            description = "Updated phone number of the company (13 digits)",
            example = "2341234567890"
    )
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Company phone number must be 13 digits")
    private String companyPhoneNumber;

    @Schema(
            description = "Updated street address of the company",
            example = "5678 New Tech Park, Lagos"
    )
    private String companyStreetAddress;

    @Schema(
            description = "Updated country where the company is located",
            example = "Nigeria"
    )
    private String companyCountry;

    @Schema(
            description = "Updated currency used by the company",
            example = "USD",
            allowableValues = {"NGN", "USD"}
    )
    private Currency companyCurrency;

    @Schema(
            description = "Unique identifier of the company (unchangeable)",
            example = "d290f1ee-6c54-4b01-90e6-d701748f0851"
    )
    private UUID companyId;

    @Schema(
            description = "Updated employer information associated with the company"
    )
    private EmployerDTO employerDTO;
}
