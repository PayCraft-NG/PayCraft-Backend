package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.service.IAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(
        name = "Authentication",
        description = "Login and token refresh for employers (JWT access + refresh tokens)."
)
@SecurityRequirements
@Slf4j @RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    // Service layer dependencies to handle authentication-related operations.
    private final IAuthenticationService authenticationService;


    /**
     * Endpoint for user login.
     * @param request contains the login credentials.
     * @return a response containing the authorization details (e.g., access authToken) if login is successful.
     */
    @Operation(
            summary = "Login",
            description = "Authenticate with email and password; returns access and refresh tokens."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping("/login")
    public ResponseEntity<DefaultApiResponse<AuthorizationResponseDto>> login(@RequestBody @Valid LoginRequestDto request){
        DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for refreshing the access authToken using a refresh authToken.
     * @param request contains the refresh authToken details.
     * @return a response containing the new authorization details (e.g., new access authToken).
     */
    @Operation(
            summary = "Refresh access token",
            description = "Exchange a valid refresh token for new access (and optionally refresh) tokens."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tokens refreshed",
                    content = @Content(schema = @Schema(implementation = DefaultApiResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Refresh token invalid or expired",
                    content = @Content(schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<DefaultApiResponse<AuthorizationResponseDto>> refreshToken(@RequestBody @Valid RefreshTokenRequestDto request){
        DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
