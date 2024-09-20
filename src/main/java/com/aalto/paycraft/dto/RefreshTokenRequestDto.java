package com.aalto.paycraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "RefreshTokenRequestDTO",
        description = "Schema for refreshing access tokens using a refresh token"
)
public record RefreshTokenRequestDto(

        @Schema(
                description = "The refresh token to renew the access token",
                example = "d290f1ee-6c54-4b01-90e6-d701748f0851"
        )
        String refreshToken
) {}
