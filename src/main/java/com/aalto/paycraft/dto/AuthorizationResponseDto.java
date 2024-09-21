package com.aalto.paycraft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "AuthorizationResponseDTO",
        description = "Schema for the response containing access and refresh tokens"
)
public record AuthorizationResponseDto(

        @Schema(
                description = "JWT access token",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,

        @Schema(
                description = "Refresh token for renewing the access token",
                example = "d290f1ee-6c54-4b01-90e6-d701748f0851"
        )
        String refreshToken,

        @Schema(
                description = "Timestamp when the token was issued",
                example = "2023-09-20T10:15:30"
        )
        String issuedAt,

        @Schema(
                description = "Validity duration of the access token in milliseconds",
                example = "3600000" // 1 hour
        )
        String accessTokenValidityTime,

        @Schema(
                description = "Validity duration of the refresh token in milliseconds",
                example = "86400000" // 1 day
        )
        String refreshTokenValidityTime
) {}
