package com.aalto.paycraft.services.impl;

import com.aalto.paycraft.dto.AuthorizationResponseDto;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.LoginRequestDto;
import com.aalto.paycraft.dto.RefreshTokenRequestDto;
import com.aalto.paycraft.entity.AuthToken;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.repository.AuthTokenRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.services.IAuthenticationService;
import com.aalto.paycraft.services.JWTService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.aalto.paycraft.constants.PayCraftConstant.*;

@Slf4j @Service @RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {
    private final EmployerRepository employerRepository;
    private final AuthTokenRepository tokenRepository;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    private record accessAndRefreshToken(String accessToken, String refreshToken) {}

    @Override
    public DefaultApiResponse<AuthorizationResponseDto> login(LoginRequestDto requestBody) {
        DefaultApiResponse<AuthorizationResponseDto> response = new DefaultApiResponse<>();
        log.info("Performing Authentication and Processing Login Request for USER with emailAddress: {}.", requestBody.emailAddress());
        try {
            // Validate the login request data
            LoginRequestDto.validate(requestBody);

            Employer employer;
            Optional<Employer> employerProfileOpt = employerRepository.findByEmailAddress(requestBody.emailAddress());

            if(employerProfileOpt.isPresent()){
                employer = employerProfileOpt.get();

                log.info("USER Found on the DB with emailAddress {}.", requestBody.emailAddress());
                if(!passwordEncoder.matches(requestBody.password(), employer.getPassword())){
                    log.warn("Invalid Password for USER {}.", requestBody.emailAddress());
                    response.setStatusCode(LOGIN_INVALID_CREDENTIALS);
                    response.setStatusMessage("Invalid Password");
                    return response;
                }
            } else {
                log.warn("USER with emailAddress {} not found in the database.", requestBody.emailAddress());
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("USER Not Found: OnBoard on the System or Verify Email");
                return response;
            }

            // Generate access and refresh tokens for the authenticated customer
            accessAndRefreshToken result = getGenerateAccessTokenAndRefreshToken(employer);

            AuthorizationResponseDto authorisationResponseDto = new AuthorizationResponseDto(
                    result.accessToken(), result.refreshToken(), getLastUpdatedAt(), "1hr","24hrs");

            // Authenticate the user with the provided credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestBody.emailAddress(), requestBody.password()));

            response.setStatusCode(LOGIN_SUCCESS);
            response.setStatusMessage("Successfully Logged In");
            response.setData(authorisationResponseDto);
            log.info("USER {} successfully logged in.", requestBody.emailAddress());

        } catch (RuntimeException ex){
            log.error("An error occurred while performing Authentication for USER {}: {}", requestBody.emailAddress(), ex.getMessage());
        }
        return response;
    }

    @Override
    public DefaultApiResponse<AuthorizationResponseDto> refreshToken(RefreshTokenRequestDto requestBody) {
        log.info("Processing Refreshing Token Request for user.");
        DefaultApiResponse<AuthorizationResponseDto> response = new DefaultApiResponse<>();

        try {
            String userEmail = jwtService.extractUsername(requestBody.refreshToken());
            log.info("Email of the Refresh Token: {}", userEmail);

            log.info("Checking if Refresh token has expired.");
            if(jwtService.isTokenExpired(requestBody.refreshToken())){
                response.setStatusCode(STATUS_400);
                response.setStatusMessage("Refresh Token Expired: User needs to Log in Again");
                log.warn("Refresh Token has expired for user {}: {}", userEmail, requestBody.refreshToken());
                return response;
            }

            Optional<Employer> existingUserAccount = employerRepository.findByEmailAddress(userEmail);
            if(existingUserAccount.isPresent()){
                Employer employer = existingUserAccount.get();

                log.info("Verifying Token is valid and properly signed for user {}.", userEmail);
                if(jwtService.isTokenValid(requestBody.refreshToken(), employer)){
                    log.info("Generating New Token for user {}.", userEmail);

                    String newAccessToken = jwtService.createJWT(employer);
                    String newRefreshToken = jwtService.generateRefreshToken(generateRefreshTokenClaims(employer), employer);

                    // Revoke old tokens and save the new tokens
                    revokeOldTokens(employer);
                    saveUserAccountToken(employer, newAccessToken, newRefreshToken);

                    response.setStatusCode(REFRESH_TOKEN_SUCCESS);
                    response.setStatusMessage("Successfully Refreshed AuthToken");
                    AuthorizationResponseDto responseDto = new AuthorizationResponseDto(
                            newAccessToken, newRefreshToken, getLastUpdatedAt(), "1hr", "24hrs");
                    response.setData(responseDto);
                } else {
                    log.warn("Invalid Token signature for user {}.", userEmail);
                }
            }
        } catch (RuntimeException ex){
            log.error("An error occurred while refreshing the token: {}", ex.getMessage());
        }
        return response;
    }

    private String getLastUpdatedAt(){
        return LocalDateTime.now().toString().replace("T", " ").substring(0, 16);
    }

    private @NotNull accessAndRefreshToken getGenerateAccessTokenAndRefreshToken(Employer employer){
        // Log the token generation process
        log.info("Generating Access Token and Refresh Token for USER");

        String jwtToken = jwtService.createJWT(employer);
        String refreshToken = jwtService.generateRefreshToken(generateRefreshTokenClaims(employer), employer);

        saveUserAccountToken(employer, jwtToken, refreshToken);
        return new accessAndRefreshToken(jwtToken, refreshToken);
    }

    private @NotNull HashMap<String, Object> generateRefreshTokenClaims(Employer employer){
        // Log the process of generating refresh token claims
        log.info("Generating Refresh Token Claims");

        HashMap<String, Object> claims = new HashMap<>();
        claims.put("username", employer.getUsername());
        claims.put("email", employer.getEmailAddress());
        claims.put("employerId", employer.getEmployerId());
        return claims;
    }

    private void saveUserAccountToken(Employer employer, String jwtToken, String refreshToken){
        // Log the process of saving tokens
        log.info("Saving tokens for USER {}", employer.getEmailAddress());

        // Save the generated access and refresh tokens for the customer
        AuthToken token = AuthToken.builder()
                .employer(employer)
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);

        // Log successful token saving
        log.info("Saved Access and Refresh tokens for USER {}", employer.getEmailAddress());
    }

    private void revokeOldTokens(Employer employer){
        // Log the process of revoking old tokens
        log.info("Revoking old tokens for customer {}", employer.getEmailAddress());

        // Revoke all old tokens for the customer
        List<AuthToken> validTokens = tokenRepository.findAllByEmployerProfile_EmployerId(employer.getEmployerId());
        if (validTokens.isEmpty()){
            log.info("No valid tokens found for customer {}.", employer.getEmailAddress());
            return;
        }
        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);

        // Log successful token revocation
        log.info("Revoked old tokens for customer {}.", employer.getEmailAddress());
    }
}