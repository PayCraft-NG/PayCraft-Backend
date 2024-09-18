package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employer;

public class CompanyMapper {
    // Convert Company entity to CompanyDTO
    public static CompanyDTO toDTO(Company company) {
        if (company == null) {
            return null;
        }

        EmployerDTO employerDTO = EmployerMapper.toDTO(company.getEmployer());

        return CompanyDTO.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companySize(company.getCompanySize())
                .companyEmailAddress(company.getCompanyEmailAddress())
                .companyPhoneNumber(company.getCompanyPhoneNumber())
                .companyStreetAddress(company.getCompanyStreetAddress())
                .companyCountry(company.getCompanyCountry())
                .companyCurrency(company.getCompanyCurrency())
                .employerDTO(employerDTO)
                .build();
    }

    // Convert CompanyDTO to Company entity
    public static Company toEntity(CompanyDTO companyDTO) {
        if (companyDTO == null) {
            return null;
        }

        Employer employer = EmployerMapper.toEntity(companyDTO.getEmployerDTO());
        Company company = new Company();
        company.setCompanyId(companyDTO.getCompanyId());
        company.setCompanyName(companyDTO.getCompanyName());
        company.setCompanySize(companyDTO.getCompanySize());
        company.setCompanyEmailAddress(companyDTO.getCompanyEmailAddress());
        company.setCompanyPhoneNumber(companyDTO.getCompanyPhoneNumber());
        company.setCompanyStreetAddress(companyDTO.getCompanyStreetAddress());
        company.setCompanyCountry(companyDTO.getCompanyCountry());
        company.setCompanyCurrency(companyDTO.getCompanyCurrency());
        company.setEmployer(employer);

        return company;
    }
}
