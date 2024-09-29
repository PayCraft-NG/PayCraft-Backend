package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.entity.Employer;

public class EmployerMapper {

    // Convert Employer entity to EmployerDTO
    public static EmployerDTO toDTO(Employer employer) {
        if (employer == null) {
            return null;
        }

        return EmployerDTO.builder()
                .firstName(employer.getFirstName())
                .lastName(employer.getLastName())
                .emailAddress(employer.getEmailAddress())
                .phoneNumber(employer.getPhoneNumber())
                .streetAddress(employer.getStreetAddress())
                .jobTitle(employer.getJobTitle())
                .bvn(employer.getBvn())
                .password(employer.getPassword())
                .employerId(employer.getEmployerId())
                .build();
    }

    // Convert EmployerDTO to Employer entity
    public static Employer toEntity(EmployerDTO employerDTO) {
        if (employerDTO == null) {
            return null;
        }

        Employer employer = new Employer();
        employer.setFirstName(employerDTO.getFirstName());
        employer.setLastName(employerDTO.getLastName());
        employer.setEmailAddress(employerDTO.getEmailAddress());
        employer.setPhoneNumber(employerDTO.getPhoneNumber());
        employer.setStreetAddress(employerDTO.getStreetAddress());
        employer.setJobTitle(employerDTO.getJobTitle());
        employer.setBvn(employerDTO.getBvn());
        employer.setPassword(employerDTO.getPassword());
        employer.setEmployerId(employerDTO.getEmployerId());

        return employer;
    }
}
