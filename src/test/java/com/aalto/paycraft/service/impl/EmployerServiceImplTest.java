package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.repository.AuthTokenRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IEmailService;
import com.aalto.paycraft.service.IVirtualAccountService;
import com.aalto.paycraft.service.JWTService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployerServiceImplTest {

    @Mock
    private EmployerRepository employerRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthTokenRepository tokenRepository;
    @Mock
    private IEmailService emailService;
    @Mock
    private JWTService jwtService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private IVirtualAccountService virtualAccountService;

    @InjectMocks
    private EmployerServiceImpl employerService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(employerService, "enableEmail", true);
        ReflectionTestUtils.setField(employerService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    @DisplayName("createEmployer succeeds even when welcome email fails")
    void createEmployerEmailFailureIsNonBlocking() {
        EmployerDTO dto = EmployerDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .emailAddress("john@example.com")
                .phoneNumber("2348012345678")
                .streetAddress("12 Main Street")
                .jobTitle("Engineer")
                .bvn("12345678901")
                .password("P@ssword1!")
                .build();
        when(passwordEncoder.encode("P@ssword1!")).thenReturn("encoded");
        when(employerRepository.save(any(Employer.class))).thenAnswer(inv -> {
            Employer e = inv.getArgument(0);
            e.setEmployerId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
            return e;
        });
        doThrow(new RuntimeException("email transport failed"))
                .when(emailService).sendEmail(any(), any(), any(), any());
        when(virtualAccountService.createVirtualAccount(any(Employer.class)))
                .thenReturn(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));

        DefaultApiResponse<EmployerDTO> response = employerService.createEmployer(dto);

        assertThat(response.getData()).isNotNull();
        assertThat(response.getData().getEmployerId())
                .isEqualTo(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        assertThat(response.getData().getVirtualAccountId())
                .isEqualTo(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        ArgumentCaptor<Employer> captor = ArgumentCaptor.forClass(Employer.class);
        verify(employerRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }
}
