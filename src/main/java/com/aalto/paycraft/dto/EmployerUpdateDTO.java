package com.aalto.paycraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
@Schema(
        name = "EmployerUpdateDTO",
        description = "Schema for updating employer information"
)
public class EmployerUpdateDTO {

    @Schema(
            description = "First name of the employer",
            example = "John"
    )
    @Size(min = 3, max = 100, message = "First name must be between 3 and 100 characters")
    private String firstName;

    @Schema(
            description = "Last name of the employer",
            example = "Doe"
    )
    @Size(min = 3, max = 100, message = "Last name must be between 3 and 100 characters")
    private String lastName;

    @Schema(
            description = "Email address of the employer",
            example = "john.doe@example.com"
    )
    @Email(message = "Email address format is invalid")
    private String emailAddress;

    @Schema(
            description = "Phone number of the employer, must be 13 digits",
            example = "23480123456789"
    )
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Phone number must be 13 digits")
    private String phoneNumber;

    @Schema(
            description = "Job title of the employer",
            example = "Manager"
    )
    @Size(min = 3, max = 100, message = "Job title must be between 3 and 100 characters")
    private String jobTitle;

    @Schema(
            description = "BVN of the employer, must be 11 digits",
            example = "12345678901"
    )
    @Pattern(regexp = "(^$|[0-9]{11})", message = "BVN must be 11 digits")
    private String bvn;

    @Schema(
            description = "Street address of the employer",
            example = "123 Main St"
    )
    private String streetAddress;
}
