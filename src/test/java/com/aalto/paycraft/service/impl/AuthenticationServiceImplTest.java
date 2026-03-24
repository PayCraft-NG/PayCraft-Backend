package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.AuthorizationResponseDto;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.LoginRequestDto;
import com.aalto.paycraft.dto.RefreshTokenRequestDto;
import com.aalto.paycraft.entity.AuthToken;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.repository.AuthTokenRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IEmailService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.aalto.paycraft.constants.PayCraftConstant.LOGIN_INVALID_CREDENTIALS;
import static com.aalto.paycraft.constants.PayCraftConstant.LOGIN_SUCCESS;
import static com.aalto.paycraft.constants.PayCraftConstant.REFRESH_TOKEN_SUCCESS;
import static com.aalto.paycraft.constants.PayCraftConstant.STATUS_400;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private EmployerRepository employerRepository;

    @Mock
    private AuthTokenRepository tokenRepository;

    @Mock
    private JWTService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private IEmailService emailService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private final UUID employerId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private final UUID companyId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private final String email = "employer@example.com";
    private final String rawPassword = "P@ssword1!";
    private final String encodedPassword = "{bcrypt}encoded";

    @BeforeEach
    void injectConfigProperties() {
        ReflectionTestUtils.setField(authenticationService, "enableEmail", false);
        ReflectionTestUtils.setField(authenticationService, "frontendUrl", "http://localhost:5173");
    }

    private Employer employerWithCompany() {
        Company company = new Company();
        company.setCompanyId(companyId);
        return Employer.builder()
                .employerId(employerId)
                .emailAddress(email)
                .password(encodedPassword)
                .firstName("Ada")
                .companies(List.of(company))
                .build();
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("returns 49 when email is not registered")
        void userNotFound() {
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.empty());

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(
                    new LoginRequestDto(email, rawPassword));

            assertThat(response.getStatusCode()).isEqualTo(STATUS_400);
            assertThat(response.getStatusMessage()).containsIgnoringCase("USER Not Found");
            assertThat(response.getData()).isNull();
            verify(authenticationManager, never()).authenticate(any());
        }

        @Test
        @DisplayName("returns 11 when password does not match")
        void wrongPassword() {
            Employer employer = employerWithCompany();
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.of(employer));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(
                    new LoginRequestDto(email, rawPassword));

            assertThat(response.getStatusCode()).isEqualTo(LOGIN_INVALID_CREDENTIALS);
            assertThat(response.getStatusMessage()).containsIgnoringCase("Invalid Password");
            assertThat(response.getData()).isNull();
            verify(jwtService, never()).createJWT(any(), any());
        }

        @Test
        @DisplayName("returns 00 and tokens when credentials are valid (no prior auth token)")
        void successFirstLogin() {
            Employer employer = employerWithCompany();
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.of(employer));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
            when(tokenRepository.findFirstByEmployer_EmployerIdOrderByCreatedAtDesc(employerId))
                    .thenReturn(Optional.empty());
            when(jwtService.createJWT(employer, companyId)).thenReturn("access-jwt");
            when(jwtService.generateRefreshToken(any(), eq(employer))).thenReturn("refresh-jwt");

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(
                    new LoginRequestDto(email, rawPassword));

            assertThat(response.getStatusCode()).isEqualTo(LOGIN_SUCCESS);
            assertThat(response.getStatusMessage()).containsIgnoringCase("Successfully Logged In");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().accessToken()).isEqualTo("access-jwt");
            assertThat(response.getData().refreshToken()).isEqualTo("refresh-jwt");

            verify(authenticationManager).authenticate(any());
            verify(emailService, never()).sendEmail(any(), any(), any(), any());

            ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
            verify(tokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getAccessToken()).isEqualTo("access-jwt");
            assertThat(tokenCaptor.getValue().getRefreshToken()).isEqualTo("refresh-jwt");
        }

        @Test
        @DisplayName("invalid email format returns status 49")
        void invalidEmailFormat() {
            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(
                    new LoginRequestDto("not-an-email", rawPassword));

            assertThat(response.getStatusCode()).isEqualTo(STATUS_400);
            assertThat(response.getData()).isNull();
            verify(employerRepository, never()).findByEmailAddress(any());
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshToken {

        @Test
        @DisplayName("returns 49 when refresh JWT is expired")
        void expiredRefreshToken() {
            when(jwtService.extractUsername("expired-refresh")).thenReturn(email);
            when(jwtService.isTokenExpired("expired-refresh")).thenReturn(true);

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(
                    new RefreshTokenRequestDto("expired-refresh"));

            assertThat(response.getStatusCode()).isEqualTo(STATUS_400);
            assertThat(response.getStatusMessage()).containsIgnoringCase("Refresh Token Expired");
            verify(employerRepository, never()).findByEmailAddress(any());
        }

        @Test
        @DisplayName("returns 05 and new tokens when refresh is valid")
        void success() {
            Employer employer = employerWithCompany();
            when(jwtService.extractUsername("good-refresh")).thenReturn(email);
            when(jwtService.isTokenExpired("good-refresh")).thenReturn(false);
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.of(employer));
            when(jwtService.isRefreshTokenValid("good-refresh", employer)).thenReturn(true);
            when(tokenRepository.findAllByEmployer_EmployerId(employerId)).thenReturn(Collections.emptyList());
            when(tokenRepository.findFirstByEmployer_EmployerIdOrderByCreatedAtDesc(employerId))
                    .thenReturn(Optional.empty());
            when(jwtService.createJWT(employer, companyId)).thenReturn("new-access");
            when(jwtService.generateRefreshToken(any(), eq(employer))).thenReturn("new-refresh");

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(
                    new RefreshTokenRequestDto("good-refresh"));

            assertThat(response.getStatusCode()).isEqualTo(REFRESH_TOKEN_SUCCESS);
            assertThat(response.getStatusMessage()).containsIgnoringCase("Successfully Refreshed");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData().accessToken()).isEqualTo("new-access");
            assertThat(response.getData().refreshToken()).isEqualTo("new-refresh");
        }

        @Test
        @DisplayName("returns 49 when employer is unknown for refresh subject")
        void employerMissingForEmail() {
            when(jwtService.extractUsername("orphan-refresh")).thenReturn("ghost@example.com");
            when(jwtService.isTokenExpired("orphan-refresh")).thenReturn(false);
            when(employerRepository.findByEmailAddress("ghost@example.com")).thenReturn(Optional.empty());

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(
                    new RefreshTokenRequestDto("orphan-refresh"));

            assertThat(response.getStatusCode()).isEqualTo(STATUS_400);
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("returns 11 when refresh signature validation fails")
        void refreshNotValidForUser() {
            Employer employer = employerWithCompany();
            when(jwtService.extractUsername("bad-refresh")).thenReturn(email);
            when(jwtService.isTokenExpired("bad-refresh")).thenReturn(false);
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.of(employer));
            when(jwtService.isRefreshTokenValid("bad-refresh", employer)).thenReturn(false);

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(
                    new RefreshTokenRequestDto("bad-refresh"));

            assertThat(response.getStatusCode()).isEqualTo(LOGIN_INVALID_CREDENTIALS);
            assertThat(response.getData()).isNull();
            verify(tokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("malformed refresh token returns status 49")
        void malformedToken() {
            when(jwtService.extractUsername("@@@")).thenThrow(new RuntimeException("invalid token"));

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.refreshToken(
                    new RefreshTokenRequestDto("@@@"));

            assertThat(response.getStatusCode()).isEqualTo(STATUS_400);
            assertThat(response.getData()).isNull();
        }
    }

    @Nested
    @DisplayName("login — company resolution edge case")
    class LoginCompanyEdgeCase {

        @Test
        @DisplayName("returns 49 when employer has no companies and no prior token")
        void noCompaniesReturnsErrorResponse() {
            Employer employer = Employer.builder()
                    .employerId(employerId)
                    .emailAddress(email)
                    .password(encodedPassword)
                    .firstName("Ada")
                    .companies(Collections.emptyList())
                    .build();
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.of(employer));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);
            when(tokenRepository.findFirstByEmployer_EmployerIdOrderByCreatedAtDesc(employerId))
                    .thenReturn(Optional.empty());

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(
                    new LoginRequestDto(email, rawPassword));

            assertThat(response.getStatusCode()).isEqualTo(STATUS_400);
            assertThat(response.getStatusMessage()).containsIgnoringCase("Unable to Retrieve Company");
            assertThat(response.getData()).isNull();
        }

        @Test
        @DisplayName("uses active company from previous access token claims when present")
        void usesCompanyFromPreviousAccessToken() {
            UUID otherCompany = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
            Employer employer = employerWithCompany();
            when(employerRepository.findByEmailAddress(email)).thenReturn(Optional.of(employer));
            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            AuthToken previous = AuthToken.builder()
                    .accessToken("old-access")
                    .refreshToken("old-refresh")
                    .employer(employer)
                    .build();
            when(tokenRepository.findFirstByEmployer_EmployerIdOrderByCreatedAtDesc(employerId))
                    .thenReturn(Optional.of(previous));

            Claims claims = org.mockito.Mockito.mock(Claims.class);
            when(claims.get("activeCompanyID")).thenReturn(otherCompany.toString());
            when(jwtService.extractClaims(eq("old-access"), any())).thenReturn(claims);
            when(jwtService.createJWT(employer, otherCompany)).thenReturn("access-2");
            when(jwtService.generateRefreshToken(any(), eq(employer))).thenReturn("refresh-2");

            DefaultApiResponse<AuthorizationResponseDto> response = authenticationService.login(
                    new LoginRequestDto(email, rawPassword));

            assertThat(response.getStatusCode()).isEqualTo(LOGIN_SUCCESS);
            verify(jwtService).createJWT(employer, otherCompany);
        }
    }
}
