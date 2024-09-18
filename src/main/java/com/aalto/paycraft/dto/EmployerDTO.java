package com.aalto.paycraft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "EmployerDTO",
        description = "Schema to hold Employer Information"
)
public class EmployerDTO {

    @Schema(
            description = "First name of the employer",
            example = "John"
    )
    @Size(min = 3, max = 100, message = "First name must be between 3 and 100 characters")
    @NotEmpty(message = "First name cannot be null or empty")
    private String firstName;

    @Schema(
            description = "Last name of the employer",
            example = "Doe"
    )
    @Size(min = 3, max = 100, message = "Last name must be between 3 and 100 characters")
    @NotEmpty(message = "Last name cannot be null or empty")
    private String lastName;

    @Schema(
            description = "Email address of the employer",
            example = "john.doe@example.com"
    )
    @Email(message = "Email address format is invalid")
    @NotEmpty(message = "Email address cannot be null or empty")
    private String emailAddress;

    @Schema(
            description = "Phone number of the employer (13 digits)",
            example = "2341234567890"
    )
    @NotEmpty(message = "Phone number cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{13})", message = "Phone number must be 13 digits")
    private String phoneNumber;

    @Schema(
            description = "Street address of the employer",
            example = "1234 Elm Street, Lagos"
    )
    @NotEmpty(message = "Street address cannot be null or empty")
    private String streetAddress;

    @Schema(
            description = "Job title of the employer",
            example = "Software Engineer"
    )
    @Size(min = 3, max = 100, message = "Job title must be between 3 and 100 characters")
    @NotEmpty(message = "Job title cannot be null or empty")
    private String jobTitle;

    @Schema(
            description = "Bank Verification Number (BVN) of the employer (11 digits)",
            example = "12345678901"
    )
    @NotEmpty(message = "BVN cannot be null or empty")
    @Pattern(regexp = "(^$|[0-9]{11})", message = "BVN must be 11 digits")
    private String bvn;

    @Schema(
            description = "Password of the employer (must contain at least one uppercase letter, one lowercase letter, one number, and one special character)",
            example = "P@ssword1!"
    )
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @NotEmpty(message = "Password cannot be null or empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String password;

    @Schema(
            description = "Unique identifier of the employer",
            example = "d290f1ee-6c54-4b01-90e6-d701748f0851"
    )
    private UUID employerId;
}
