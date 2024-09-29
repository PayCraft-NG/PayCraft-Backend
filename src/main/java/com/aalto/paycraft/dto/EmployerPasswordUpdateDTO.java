package com.aalto.paycraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
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
@Schema(
        name = "EmployerPasswordUpdateDTO",
        description = "Schema for updating an employer's password"
)
public class EmployerPasswordUpdateDTO {

    @Schema(
            description = "Current password of the employer",
            example = "OldP@ssword1!"
    )
    @Size(min = 8, message = "Old Password must be at least 8 characters long")
    @NotEmpty(message = "Old Password cannot be null or empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Old Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String oldPassword;

    @Schema(
            description = "New password of the employer",
            example = "NewP@ssword1!"
    )
    @Size(min = 8, message = "New Password must be at least 8 characters long")
    @NotEmpty(message = "New Password cannot be null or empty")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "New Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
    )
    private String newPassword;
}
