package com.aalto.paycraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "LoginRequestDTO",
        description = "Schema for the login request with user credentials"
)
public record LoginRequestDto(

        @Schema(
                description = "Email address of the user",
                example = "user@example.com"
        )
        String emailAddress,

        @Schema(
                description = "Password of the user",
                example = "securePassword123"
        )
        String password
) {
    public static void validate(LoginRequestDto dto) {
        if (dto.emailAddress == null || !dto.emailAddress.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("A valid emailAddress is required.");
        }
    }
}
