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

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements ICompanyService {
    private static final Logger log = LoggerFactory.getLogger(CompanyServiceImpl.class);
    private final CompanyRepository companyRepository;
    private final EmployerRepository employerRepository;
    private final JWTService jwtService;
    private final HttpServletRequest request;

    // Gets the AccessToken from the Request Sent
    private String EMPLOYER_ACCESS_TOKEN(){
        return request.getHeader("Authorization").substring(7);
    }

    // Get the ID of the employer making the request
    private UUID GET_ID(String parameter){
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity());  // Function.identity() returns the same object
        return UUID.fromString((String) claims.get(parameter));
    }

    @Override
    public DefaultApiResponse<CompanyDTO> createCompany(CompanyDTO companyDTO, UUID employerId) {
        DefaultApiResponse<CompanyDTO> response = new DefaultApiResponse<>();
        // The longest function name in history
        verifyRecordByCompanyNameAndEmployerId(companyDTO.getCompanyName(), employerId);
        Employer employer = verifyAndFetchEmployerById(employerId);

        Company company = CompanyMapper.toEntity(companyDTO);
        company.setEmployer(employer);

        log.info("Company {}", company);
        companyRepository.save(company);

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
        Company company = verifyAndFetchCompanyById(GET_ID("activeCompanyID"));
        Employer employer = verifyAndFetchEmployerById(company.getEmployer().getEmployerId());

        CompanyDTO companyDTO = CompanyMapper.toDTO(company);
        companyDTO.setEmployerDTO(
                EmployerDTO.builder()
                        .employerId(employer.getEmployerId())
                        .phoneNumber(employer.getPhoneNumber())
                        .emailAddress(employer.getEmailAddress())
                        .build()
        );

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Company details");
        response.setData(companyDTO);
        return response;
    }

    @Override
    public DefaultApiResponse<List<CompanyDTO>> getCompaniesByEmployerId(Integer page, Integer pageSize) {
        DefaultApiResponse<List<CompanyDTO>> response = new DefaultApiResponse<>();
        Employer employer = verifyAndFetchEmployerById(GET_ID("userID"));
        Pageable pageable = PageRequest.of(page, pageSize);

        List<Company> companyList = companyRepository.findAllByEmployer_EmployerId(employer.getEmployerId(), pageable);

        List<CompanyDTO> responseList = companyList.stream()
                .map(company ->
                        CompanyDTO.builder()
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
        response.setStatusMessage("List of Companies");
        response.setData(responseList);
        return response;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> updateCompany(CompanyUpdateDTO companyUpdateDTO) {
        DefaultApiResponse<CompanyDTO> response = new DefaultApiResponse<>();
        Company company = verifyAndFetchCompanyById(GET_ID("activeCompanyID"));

        companyRepository.save(updateCompanyRecord(company, companyUpdateDTO));

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
        Company company = verifyAndFetchCompanyById(GET_ID("activeCompanyID"));

        companyRepository.delete(company);

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

    private Company updateCompanyRecord(Company company, CompanyUpdateDTO companyUpdateDTO){
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

        // Ensure the newly updated attributes do not exist already
        if(companyUpdateDTO.getCompanyEmailAddress() != null) {
            if(companyRepository.existsByCompanyEmailAddress(companyUpdateDTO.getCompanyEmailAddress()))
                throw new EmployerAlreadyExists("Company account already registered with this email address: " + companyUpdateDTO.getCompanyEmailAddress());
            company.setCompanyEmailAddress(companyUpdateDTO.getCompanyEmailAddress());
        }
        if(companyUpdateDTO.getCompanyPhoneNumber() != null) {
            if (companyRepository.existsByCompanyPhoneNumber(companyUpdateDTO.getCompanyPhoneNumber()))
                throw new EmployerAlreadyExists("Company account already registered with this phone number: " + companyUpdateDTO.getCompanyPhoneNumber());
            company.setCompanyPhoneNumber(companyUpdateDTO.getCompanyPhoneNumber());
        }
        return company;
    }

    private Company verifyAndFetchCompanyById(UUID companyId){
        return companyRepository.findById(companyId).orElseThrow(
                ()-> new RuntimeException("Company ID does not exist: " + companyId)
        );
    }

    private void verifyRecordByCompanyNameAndEmployerId(String companyName, UUID employerId){
        if(companyRepository.existsByCompanyNameAndEmployer_EmployerId(companyName, employerId))
            throw new RuntimeException("Company already already registered with this company name: " + companyName);
    }

    private Employer verifyAndFetchEmployerById(UUID employerId){
        return employerRepository.findById(employerId).orElseThrow(
                () -> new RuntimeException("Employer ID does not exist: " + employerId)
        );
    }

    /* Method to Verify Token Expiration */
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }
}
