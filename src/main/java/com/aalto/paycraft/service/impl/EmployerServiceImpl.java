package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.dto.EmployerUpdateDTO;
import com.aalto.paycraft.entity.AuthToken;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.exception.EmployerAlreadyExists;
import com.aalto.paycraft.exception.EmployerNotFound;
import com.aalto.paycraft.exception.PasswordUpdateException;
import com.aalto.paycraft.mapper.EmployerMapper;
import com.aalto.paycraft.repository.AuthTokenRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IEmailService;
import com.aalto.paycraft.service.IEmployerService;
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

    // Gets the AccessToken from the Request Sent
    private String EMPLOYER_ACCESS_TOKEN(){
        return request.getHeader("Authorization").substring(7);
    }

    // Get the ID of the employer making the request
    private UUID EMPLOYER_ID(){
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity());  // Function.identity() returns the same object
        return UUID.fromString((String) claims.get("userID"));
    }

    @Value("${spring.mail.enable}")
    private Boolean enableEmail;

    @Override
    public DefaultApiResponse<EmployerDTO> createEmployer(EmployerDTO employerDTO) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();

        verifyRecordPhoneNumberAndEmailAddress(employerDTO);

        // Map DTO to entity and encrypt the password
        Employer employer = EmployerMapper.toEntity(employerDTO);
        employer.setPassword(passwordEncoder.encode(employerDTO.getPassword()));

        // Save the employer profile
        employerRepository.save(employer);

        log.info("===== EmailService status: {} =====", enableEmail);
        if (enableEmail){
            log.info("Emails Works");
//            emailService.sendEmail(employer.getEmailAddress(),
//                    "Sign up Success!",
//                    createEmailContext(employer.getFirstName()),
//                    "signup");
        }

        response.setStatusCode(PayCraftConstant.ONBOARD_SUCCESS);
        response.setStatusMessage("Employer created successfully");
        // Build the response DTO with saved data
        response.setData(
                EmployerDTO.builder()
                        .employerId(employer.getEmployerId())
                        .firstName(employer.getFirstName())
                        .lastName(employer.getLastName())
                        .emailAddress(employer.getEmailAddress())
                        .build()
        );
        return response;

    }

    @Override
    public DefaultApiResponse<EmployerDTO> getEmployer() {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(EMPLOYER_ID());
        EmployerDTO employerDTO = EmployerMapper.toDTO(employer);
        // Prevent the password and bvn from being returned
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
        updateRecord(employer,employerUpdateDTO);
        employerRepository.save(employer);

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employer updated successfully");
        // Only the employer ID and updated attributes are returned
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

        // Soft delete it is
        // However, there is a bug that prevents recreating the exact same account after it has been deleted.
        // It's not a bug, it's a feature, wink!
        employer.setDeleted(true);
        revokeAllTokens(employer);
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

        if(Objects.equals(employerPasswordUpdateDTO.getOldPassword(), employerPasswordUpdateDTO.getNewPassword()))
            throw new PasswordUpdateException("New password must be different from the old password");

        if(!passwordEncoder.matches(employerPasswordUpdateDTO.getOldPassword(), employer.getPassword()))
            throw new PasswordUpdateException("Old password is incorrect");

        log.info("Employer password (before update): {}", employer.getPassword());

        employerRepository.save(updatePassword(employer, employerPasswordUpdateDTO.getNewPassword()));

        log.info("Employer password (after update): {}", employer.getPassword());

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee password updated successfully");
        response.setData(
                EmployerDTO.builder()
                        .employerId(employer.getEmployerId())
                        .phoneNumber(employer.getPhoneNumber())
                        .emailAddress(employer.getEmailAddress())
                        .build()
        );
        return response;
    }

    private Employer updatePassword(Employer employer, String password) {
        // Encrypt and update the password
        employer.setPassword(passwordEncoder.encode(password));
        return employer;
    }

    private Employer verifyAndFetchById(UUID employerId){
        return employerRepository.findById(employerId).orElseThrow(
                () -> new EmployerNotFound("Employer ID does not exist: " + employerId)
        );
    }

    private void verifyRecordPhoneNumberAndEmailAddress(EmployerDTO employerDTO) {
        // Verify that the phone number or email address or BVN doesn't already exist
        if(employerRepository.existsByPhoneNumber(employerDTO.getPhoneNumber()))
            throw new PasswordUpdateException("Employer account already registered with this phone number: " + employerDTO.getPhoneNumber());

        if(employerRepository.existsByEmailAddress(employerDTO.getEmailAddress()))
            throw new PasswordUpdateException("Employer account already registered with this email address: " + employerDTO.getEmailAddress());

        if(employerRepository.existsByBvn(employerDTO.getBvn()))
            throw new PasswordUpdateException("Employer account already registered with this BVN: " + employerDTO.getBvn());
    }

    private void updateRecord(Employer destEmployer, EmployerUpdateDTO srcEmployerDTO) {
        // Update all non-null fields
        // Note: EmployerUpdateDTO, it is similar to EmployerDTO but attributes can be null
        if(srcEmployerDTO.getFirstName() != null)
            destEmployer.setFirstName(srcEmployerDTO.getFirstName());
        if(srcEmployerDTO.getLastName() != null)
            destEmployer.setLastName(srcEmployerDTO.getLastName());
        if(srcEmployerDTO.getJobTitle() != null)
            destEmployer.setJobTitle(srcEmployerDTO.getJobTitle());
        if(srcEmployerDTO.getStreetAddress() != null)
            destEmployer.setStreetAddress(srcEmployerDTO.getStreetAddress());

        // Ensure the newly updated attributes do not exist already
        if (srcEmployerDTO.getEmailAddress() !=null && !Objects.equals(
                srcEmployerDTO.getEmailAddress(), destEmployer.getEmailAddress())) {
            if (employerRepository.existsByEmailAddress(srcEmployerDTO.getEmailAddress()))
                throw new RuntimeException("Employer account already registered with this email address: " + srcEmployerDTO.getEmailAddress());
            destEmployer.setEmailAddress(srcEmployerDTO.getEmailAddress());
        }

        if(srcEmployerDTO.getPhoneNumber() != null && !Objects.equals(
                srcEmployerDTO.getPhoneNumber(), destEmployer.getPhoneNumber())) {
            if (employerRepository.existsByPhoneNumber(srcEmployerDTO.getPhoneNumber()))
                throw new EmployerAlreadyExists("Employer account already registered with this phone number: " + srcEmployerDTO.getPhoneNumber());
            destEmployer.setPhoneNumber(srcEmployerDTO.getPhoneNumber());
        }
    }

    private static Context createEmailContext(String firstName){
        Context emailContext = new Context();
        emailContext.setVariable("name", firstName);
        return emailContext;
    }

    /* Method to Verify Token Expiration */
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    // Revoke all tokens related to employer since they are no longer on the system
    private void revokeAllTokens(Employer employer){
        // Log the process of revoking old tokens
        log.info("Revoking old tokens for employer {}", employer.getEmailAddress());

        // Revoke all old tokens for the customer
        List<AuthToken> validTokens = tokenRepository.findAllByEmployer_EmployerId(employer.getEmployerId());
        if (validTokens.isEmpty()){
            log.info("No valid tokens found for employer with email {}.", employer.getEmailAddress());
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
