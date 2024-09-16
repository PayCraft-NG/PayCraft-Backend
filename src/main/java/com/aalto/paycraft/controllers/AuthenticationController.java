package com.aalto.paycraft.controllers;

import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.services.IAuthenticationService;
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
    @PostMapping("/refresh-token")
    public ResponseEntity<DefaultApiResponse<AuthorizationResponseDto>> refreshToken(@RequestBody @Valid RefreshTokenRequestDto request){
        DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
