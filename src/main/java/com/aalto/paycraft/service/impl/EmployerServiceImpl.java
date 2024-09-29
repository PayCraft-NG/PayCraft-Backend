package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.dto.EmployerUpdateDTO;
import com.aalto.paycraft.entity.AuthToken;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.entity.VirtualAccount;
import com.aalto.paycraft.exception.EmployerAlreadyExists;
import com.aalto.paycraft.exception.EmployerNotFound;
import com.aalto.paycraft.exception.PasswordUpdateException;
import com.aalto.paycraft.mapper.EmployerMapper;
import com.aalto.paycraft.repository.AuthTokenRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IEmailService;
import com.aalto.paycraft.service.IEmployerService;
import com.aalto.paycraft.service.IVirtualAccountService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements IEmployerService {
    private static final Logger log = LoggerFactory.getLogger(EmployerServiceImpl.class);
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenRepository tokenRepository;
    private final IEmailService emailService;
    private final JWTService jwtService;
    private final HttpServletRequest request;
    private final IVirtualAccountService virtualAccountService;

    // Gets the AccessToken from the request header
    private String EMPLOYER_ACCESS_TOKEN() {
        return request.getHeader("Authorization").substring(7); // Removes "Bearer " prefix
    }

    // Get the ID of the employer making the request
    private UUID EMPLOYER_ID() {
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity());
        return UUID.fromString((String) claims.get("userID"));
    }

    @Value("${spring.mail.enable}")
    private Boolean enableEmail;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public DefaultApiResponse<EmployerDTO> createEmployer(EmployerDTO employerDTO) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();

        // Verify uniqueness of phone number, email, and BVN
        verifyRecordPhoneNumberAndEmailAddress(employerDTO);

        // Map DTO to entity and encrypt the password
        Employer employer = EmployerMapper.toEntity(employerDTO);
        employer.setPassword(passwordEncoder.encode(employerDTO.getPassword()));

        // Save the employer profile
        employerRepository.save(employer);
        //====== Email Service ======//
        if (enableEmail){
            log.info("===== Email Enabled =====");
           emailService.sendEmail(employer.getEmailAddress(),
                   "Welcome to PayCraft",
                   createEmailContext(employer.getFirstName(), frontendUrl),
                   "signup");
        }
        else
            log.info("===== Email Disabled =====");
        //====== Email Service ======//

        // ======= Virtual Account Service  ========= //
        UUID virtualAccountId = virtualAccountService.createVirtualAccount(employer);


        // Set response details
        response.setStatusCode(PayCraftConstant.ONBOARD_SUCCESS);
        response.setStatusMessage("Employer created successfully");
        response.setData(
                EmployerDTO.builder()
                        .employerId(employer.getEmployerId())
                        .firstName(employer.getFirstName())
                        .lastName(employer.getLastName())
                        .emailAddress(employer.getEmailAddress())
                        .virtualAccountId(virtualAccountId)
                        .build()
        );
        return response;
    }

    @Override
    public DefaultApiResponse<EmployerDTO> getEmployer() {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(EMPLOYER_ID());
        EmployerDTO employerDTO = EmployerMapper.toDTO(employer);

        // Prevent returning sensitive information
        employerDTO.setPassword(null);
        employerDTO.setBvn(null);

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employer details");
        response.setData(employerDTO);
        return response;
    }

    @Override
    @Transactional
    public DefaultApiResponse<EmployerDTO> updateEmployer(EmployerUpdateDTO employerUpdateDTO) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(EMPLOYER_ID());
        updateRecord(employer, employerUpdateDTO); // Update employer details
        employerRepository.save(employer);

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employer updated successfully");
        response.setData(EmployerDTO.builder()
                .employerId(employer.getEmployerId())
                .phoneNumber(employer.getPhoneNumber())
                .emailAddress(employer.getEmailAddress())
                .firstName(employerUpdateDTO.getFirstName())
                .lastName(employerUpdateDTO.getLastName())
                .jobTitle(employerUpdateDTO.getJobTitle())
                .streetAddress(employerUpdateDTO.getStreetAddress())
                .build());
        return response;
    }

    @Override
    @Transactional
    public DefaultApiResponse<EmployerDTO> deleteEmployer() {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(EMPLOYER_ID());

        // Soft delete the employer
        employer.setDeleted(true);
        employer.setEmailAddress(employer.getEmailAddress() + "_deleted_" + UUID.randomUUID());
        employer.setPhoneNumber(employer.getPhoneNumber() + "_deleted_" + UUID.randomUUID());
        employer.setBvn(employer.getBvn() + "_deleted_" + UUID.randomUUID());
        revokeAllTokens(employer); // Revoke tokens
        employerRepository.save(employer);

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employer deleted successfully");
        response.setData(EmployerDTO.builder()
                .employerId(employer.getEmployerId())
                .phoneNumber(employer.getPhoneNumber())
                .emailAddress(employer.getEmailAddress())
                .build()
        );
        return response;
    }

    @Override
    @Transactional
    public DefaultApiResponse<EmployerDTO> updateEmployerPassword(EmployerPasswordUpdateDTO employerPasswordUpdateDTO) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(EMPLOYER_ID());

        // Validate new password against old password
        if (Objects.equals(employerPasswordUpdateDTO.getOldPassword(), employerPasswordUpdateDTO.getNewPassword())) {
            throw new PasswordUpdateException("New password must be different from the old password");
        }

        // Check if the old password matches
        if (!passwordEncoder.matches(employerPasswordUpdateDTO.getOldPassword(), employer.getPassword())) {
            throw new PasswordUpdateException("Old password is incorrect");
        }

        log.info("Updating password for employer ID: {}", employer.getEmployerId());
        employerRepository.save(updatePassword(employer, employerPasswordUpdateDTO.getNewPassword()));

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employer password updated successfully");
        response.setData(
                EmployerDTO.builder()
                        .employerId(employer.getEmployerId())
                        .phoneNumber(employer.getPhoneNumber())
                        .emailAddress(employer.getEmailAddress())
                        .build()
        );
        return response;
    }

    // Helper method to encrypt and update the password
    private Employer updatePassword(Employer employer, String password) {
        employer.setPassword(passwordEncoder.encode(password));
        return employer;
    }

    // Fetch employer by ID and throw an exception if not found
    private Employer verifyAndFetchById(UUID employerId) {
        return employerRepository.findById(employerId).orElseThrow(
                () -> new EmployerNotFound("Employer ID does not exist: " + employerId)
        );
    }

    // Verify uniqueness of phone number, email, and BVN
    private void verifyRecordPhoneNumberAndEmailAddress(EmployerDTO employerDTO) {
        if (employerRepository.existsByPhoneNumber(employerDTO.getPhoneNumber())) {
            throw new PasswordUpdateException("Employer account already registered with this phone number: " + employerDTO.getPhoneNumber());
        }

        if (employerRepository.existsByEmailAddress(employerDTO.getEmailAddress())) {
            throw new PasswordUpdateException("Employer account already registered with this email address: " + employerDTO.getEmailAddress());
        }

        if (employerRepository.existsByBvn(employerDTO.getBvn())) {
            throw new PasswordUpdateException("Employer account already registered with this BVN: " + employerDTO.getBvn());
        }
    }

    // Update non-null fields of the employer
    private void updateRecord(Employer destEmployer, EmployerUpdateDTO srcEmployerDTO) {
        if (srcEmployerDTO.getFirstName() != null)
            destEmployer.setFirstName(srcEmployerDTO.getFirstName());
        if (srcEmployerDTO.getLastName() != null)
            destEmployer.setLastName(srcEmployerDTO.getLastName());
        if (srcEmployerDTO.getJobTitle() != null)
            destEmployer.setJobTitle(srcEmployerDTO.getJobTitle());
        if (srcEmployerDTO.getStreetAddress() != null)
            destEmployer.setStreetAddress(srcEmployerDTO.getStreetAddress());

        // Ensure the updated email does not already exist
        if (srcEmployerDTO.getEmailAddress() != null && !Objects.equals(
                srcEmployerDTO.getEmailAddress(), destEmployer.getEmailAddress())) {
            if (employerRepository.existsByEmailAddress(srcEmployerDTO.getEmailAddress())) {
                throw new RuntimeException("Employer account already registered with this email address: " + srcEmployerDTO.getEmailAddress());
            }
            destEmployer.setEmailAddress(srcEmployerDTO.getEmailAddress());
        }

        // Ensure the updated phone number does not already exist
        if (srcEmployerDTO.getPhoneNumber() != null && !Objects.equals(
                srcEmployerDTO.getPhoneNumber(), destEmployer.getPhoneNumber())) {
            if (employerRepository.existsByPhoneNumber(srcEmployerDTO.getPhoneNumber())) {
                throw new EmployerAlreadyExists("Employer account already registered with this phone number: " + srcEmployerDTO.getPhoneNumber());
            }
            destEmployer.setPhoneNumber(srcEmployerDTO.getPhoneNumber());
        }
    }
  
    private static Context createEmailContext(String firstName, String frontendUrl){
        Context emailContext = new Context();
        emailContext.setVariable("username", firstName);
        emailContext.setVariable("paycraftURL", frontendUrl);
        return emailContext;
    }

    // Verify token expiration
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired for employer ID: {}", EMPLOYER_ID());
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    // Revoke all tokens related to the employer
    private void revokeAllTokens(Employer employer) {
        log.info("Revoking old tokens for employer {}", employer.getEmailAddress());
        List<AuthToken> validTokens = tokenRepository.findAllByEmployer_EmployerId(employer.getEmployerId());

        if (validTokens.isEmpty()) {
            log.info("No valid tokens found for employer with email {}.", employer.getEmailAddress());
            return;
        }

        validTokens.forEach(token -> {
            token.setRevoked(true);
            token.setExpired(true);
        });
        tokenRepository.saveAll(validTokens);
        log.info("Revoked old tokens for employer {}.", employer.getEmailAddress());
    }
}
