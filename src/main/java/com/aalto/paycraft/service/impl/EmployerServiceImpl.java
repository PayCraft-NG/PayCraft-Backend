package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.dto.EmployerUpdateDTO;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.exception.EmployerAlreadyExists;
import com.aalto.paycraft.exception.EmployerNotFound;
import com.aalto.paycraft.exception.PasswordUpdateException;
import com.aalto.paycraft.mapper.EmployerMapper;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IEmployerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements IEmployerService {
    private static final Logger log = LoggerFactory.getLogger(EmployerServiceImpl.class);
    private final EmployerRepository employerRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public DefaultApiResponse<EmployerDTO> createEmployer(EmployerDTO employerDTO) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();

        verifyRecordPhoneNumberAndEmailAddress(employerDTO);

        // Map DTO to entity and encrypt the password
        Employer employer = EmployerMapper.toEntity(employerDTO);
        employer.setPassword(passwordEncoder.encode(employerDTO.getPassword()));

        // Save the employer profile
        employerRepository.save(employer);

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
    public DefaultApiResponse<EmployerDTO> getEmployer(UUID employerId) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(employerId);
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
    public DefaultApiResponse<EmployerDTO> updateEmployer(EmployerUpdateDTO employerUpdateDTO, UUID employerId) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(employerId);

        log.info("Employer (before update): {}", employer);
        updateRecord(employer,employerUpdateDTO);

        log.info("Employer (after update): {}", employer);
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
    public DefaultApiResponse<EmployerDTO> deleteEmployer(UUID employerId) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(employerId);

        // Soft delete it is
        // However, there is a bug that prevents recreating the exact same account after it has been deleted.
        // It's not a bug, it's a feature, wink!
        employer.setDeleted(true);
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
    public DefaultApiResponse<EmployerDTO> updateEmployerPassword(EmployerPasswordUpdateDTO employerPasswordUpdateDTO, UUID employerId) {
        DefaultApiResponse<EmployerDTO> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchById(employerId);

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
        if(srcEmployerDTO.getEmailAddress() != null) {
            if(employerRepository.existsByEmailAddress(srcEmployerDTO.getEmailAddress()))
                throw new EmployerAlreadyExists("Employer account already registered with this email address: " + srcEmployerDTO.getEmailAddress());
            destEmployer.setEmailAddress(srcEmployerDTO.getEmailAddress());
        }
        if(srcEmployerDTO.getPhoneNumber() != null) {
            if (employerRepository.existsByPhoneNumber(srcEmployerDTO.getPhoneNumber()))
                throw new EmployerAlreadyExists("Employer account already registered with this phone number: " + srcEmployerDTO.getPhoneNumber());
            destEmployer.setPhoneNumber(srcEmployerDTO.getPhoneNumber());
        }
    }
}
