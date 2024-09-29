package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.CompanyUpdateDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employer;
import com.aalto.paycraft.exception.EmployerAlreadyExists;
import com.aalto.paycraft.mapper.CompanyMapper;
import com.aalto.paycraft.repository.CompanyRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.ICompanyService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.aalto.paycraft.constants.PayCraftConstant.STATUS_400;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements ICompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);
    private final CompanyRepository companyRepository;
    private final EmployerRepository employerRepository;
    private final JWTService jwtService;
    private final HttpServletRequest request;

    // Extract the Access Token from the request
    private String EMPLOYER_ACCESS_TOKEN() {
        return request.getHeader("Authorization").substring(7); // Remove "Bearer " prefix
    }

    // Extract employer-related ID from the JWT token claims
    private UUID GET_ID(String parameter) {
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity()); // Get all claims
        return UUID.fromString((String) claims.get(parameter)); // Get specific claim by parameter
    }

    @Override
    public DefaultApiResponse<CompanyDTO> createCompany(CompanyDTO companyDTO, UUID employerId) {
        DefaultApiResponse<CompanyDTO> response = new DefaultApiResponse<>();

        // Check if the company already exists for this employer
        verifyRecordByCompanyNameAndEmployerId(companyDTO.getCompanyName(), employerId);

        // Fetch employer information and create a new company
        Employer employer = verifyAndFetchEmployerById(employerId);
        Company company = CompanyMapper.toEntity(companyDTO);
        company.setEmployer(employer);

        log.info("Creating company: {}", company);
        companyRepository.save(company); // Save company to the repository

        // Prepare the response with company and employer details
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Company created successfully");
        response.setData(
                CompanyDTO.builder()
                        .companyId(company.getCompanyId())
                        .companyName(company.getCompanyName())
                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                        .companyEmailAddress(company.getCompanyEmailAddress())
                        .employerDTO(
                                EmployerDTO.builder()
                                        .employerId(employer.getEmployerId())
                                        .phoneNumber(employer.getPhoneNumber())
                                        .emailAddress(employer.getEmailAddress())
                                        .build()
                        )
                        .build()
        );

        return response;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> getCompany() {
        DefaultApiResponse<CompanyDTO> response = new DefaultApiResponse<>();

        // Fetch the active company based on the token claims
        Company company = verifyAndFetchCompanyById(GET_ID("activeCompanyID"));
        Employer employer = verifyAndFetchEmployerById(company.getEmployer().getEmployerId());

        // Prepare the response with company and employer details
        CompanyDTO companyDTO = CompanyMapper.toDTO(company);
        companyDTO.setEmployerDTO(
                EmployerDTO.builder()
                        .employerId(employer.getEmployerId())
                        .phoneNumber(employer.getPhoneNumber())
                        .emailAddress(employer.getEmailAddress())
                        .build()
        );

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Company details retrieved successfully");
        response.setData(companyDTO);
        return response;
    }

    @Override
    public DefaultApiResponse<List<CompanyDTO>> getCompaniesByEmployerId(Integer page, Integer pageSize) {
        DefaultApiResponse<List<CompanyDTO>> response = new DefaultApiResponse<>();

        // Get employer ID from token claims and fetch employer data
        Employer employer = verifyAndFetchEmployerById(GET_ID("userID"));
        Pageable pageable = PageRequest.of(page, pageSize); // Set pagination

        // Retrieve a paginated list of companies for the employer
        List<Company> companyList = companyRepository.findAllByEmployer_EmployerId(employer.getEmployerId(), pageable);

        // Convert the list of companies to DTOs
        List<CompanyDTO> responseList = companyList.stream()
                .map(company -> CompanyDTO.builder()
                        .companyId(company.getCompanyId())
                        .companyName(company.getCompanyName())
                        .companySize(company.getCompanySize())
                        .companyCountry(company.getCompanyCountry())
                        .companyEmailAddress(company.getCompanyEmailAddress())
                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                        .companyStreetAddress(company.getCompanyStreetAddress())
                        .companyCurrency(company.getCompanyCurrency())
                        .employerDTO(
                                EmployerDTO.builder()
                                        .employerId(employer.getEmployerId())
                                        .phoneNumber(employer.getPhoneNumber())
                                        .emailAddress(employer.getEmailAddress())
                                        .build()
                        )
                        .build()
                ).toList();

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("List of Companies retrieved successfully");
        response.setData(responseList);
        return response;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> updateCompany(CompanyUpdateDTO companyUpdateDTO) {
        DefaultApiResponse<CompanyDTO> response = new DefaultApiResponse<>();

        // Fetch the active company from token claims
        Company company = verifyAndFetchCompanyById(GET_ID("activeCompanyID"));

        // Update the company record with new details
        companyRepository.save(updateCompanyRecord(company, companyUpdateDTO));

        // Prepare the response with updated company details
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Company updated successfully");
        response.setData(
                CompanyDTO.builder()
                        .companyId(company.getCompanyId())
                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                        .companyEmailAddress(company.getCompanyEmailAddress())
                        .companyName(companyUpdateDTO.getCompanyName())
                        .companySize(companyUpdateDTO.getCompanySize())
                        .companyCountry(companyUpdateDTO.getCompanyCountry())
                        .companyCurrency(companyUpdateDTO.getCompanyCurrency())
                        .companyStreetAddress(companyUpdateDTO.getCompanyStreetAddress())
                        .build()
        );
        return response;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> deleteCompany() {
        DefaultApiResponse<CompanyDTO> response = new DefaultApiResponse<>();

        // Fetch the active company from token claims
        Company company = verifyAndFetchCompanyById(GET_ID("activeCompanyID"));


        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity()); // Get all claims
        List<String> companyIds = (List<String>) claims.get("companyIds");
        if(companyIds.size() > 1) {
            companyRepository.delete(company);
        }else{
            response.setStatusCode(STATUS_400);
            response.setStatusMessage("Cannot delete the only existing company");
            return response;
        }

        // Delete the company from the repository
        companyRepository.delete(company);

        // Prepare the response confirming the deletion
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Company deleted successfully");
        response.setData(
                CompanyDTO.builder()
                        .companyId(company.getCompanyId())
                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                        .companyEmailAddress(company.getCompanyEmailAddress())
                        .build()
        );
        return response;
    }

    // Helper method to update a company's details
    private Company updateCompanyRecord(Company company, CompanyUpdateDTO companyUpdateDTO) {
        // Update company fields only if the new value is not null
        if (companyUpdateDTO.getCompanyName() != null)
            company.setCompanyName(companyUpdateDTO.getCompanyName());
        if (companyUpdateDTO.getCompanySize() != null)
            company.setCompanySize(companyUpdateDTO.getCompanySize());
        if (companyUpdateDTO.getCompanyStreetAddress() != null)
            company.setCompanyStreetAddress(companyUpdateDTO.getCompanyStreetAddress());
        if (companyUpdateDTO.getCompanyCountry() != null)
            company.setCompanyCountry(companyUpdateDTO.getCompanyCountry());
        if (companyUpdateDTO.getCompanyCurrency() != null)
            company.setCompanyCurrency(companyUpdateDTO.getCompanyCurrency());

        // Check for unique email and phone number constraints before updating
        if (companyUpdateDTO.getCompanyEmailAddress() != null) {
            if (companyRepository.existsByCompanyEmailAddress(companyUpdateDTO.getCompanyEmailAddress()))
                throw new EmployerAlreadyExists("Company with email already exists: " + companyUpdateDTO.getCompanyEmailAddress());
            company.setCompanyEmailAddress(companyUpdateDTO.getCompanyEmailAddress());
        }
        if (companyUpdateDTO.getCompanyPhoneNumber() != null) {
            if (companyRepository.existsByCompanyPhoneNumber(companyUpdateDTO.getCompanyPhoneNumber()))
                throw new EmployerAlreadyExists("Company with phone number already exists: " + companyUpdateDTO.getCompanyPhoneNumber());
            company.setCompanyPhoneNumber(companyUpdateDTO.getCompanyPhoneNumber());
        }
        return company;
    }

    // Helper method to fetch company by ID, throws exception if not found
    private Company verifyAndFetchCompanyById(UUID companyId) {
        return companyRepository.findById(companyId).orElseThrow(
                () -> new RuntimeException("Company ID does not exist: " + companyId)
        );
    }

    // Ensure company with given name does not exist for the employer
    private void verifyRecordByCompanyNameAndEmployerId(String companyName, UUID employerId) {
        if (companyRepository.existsByCompanyNameAndEmployer_EmployerId(companyName, employerId))
            throw new RuntimeException("Company already registered with this name: " + companyName);
    }

    // Helper method to fetch employer by ID, throws exception if not found
    private Employer verifyAndFetchEmployerById(UUID employerId) {
        return employerRepository.findById(employerId).orElseThrow(
                () -> new RuntimeException("Employer ID does not exist: " + employerId)
        );
    }

    // Helper method to verify JWT token expiration
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("JWT token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }
}
