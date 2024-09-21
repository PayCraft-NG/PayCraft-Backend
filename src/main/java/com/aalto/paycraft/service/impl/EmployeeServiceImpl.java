package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employee;
import com.aalto.paycraft.mapper.EmployeeMapper;
import com.aalto.paycraft.repository.CompanyRepository;
import com.aalto.paycraft.repository.EmployeeRepository;
import com.aalto.paycraft.service.IEmployeeService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service @RequiredArgsConstructor @Slf4j
public class EmployeeServiceImpl implements IEmployeeService {
    private final HttpServletRequest request;
    private final JWTService jwtService;
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    // Gets the AccessToken from the Request Sent
    private String EMPLOYER_ACCESS_TOKEN(){
        return request.getHeader("Authorization").substring(7);
    }

    // Get the current company the employee is being created under
    private UUID COMPANY_ID(){
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(EMPLOYER_ACCESS_TOKEN(), Function.identity());  // Function.identity() returns the same object
        return UUID.fromString((String) claims.get("activeCompanyID"));
    }

    @Override
    public DefaultApiResponse<EmployeeDto> createEmployee(EmployeeRequestDto requestBody) {
        verifyTokenExpiration(EMPLOYER_ACCESS_TOKEN());
        DefaultApiResponse<EmployeeDto> response = new DefaultApiResponse<>();

        // Verify if the record already exists
        verifyRecord(requestBody, COMPANY_ID());

        // Verify and fetch the company profile using the UUID
        Company company = verifyAndFetchCompanyUUID(COMPANY_ID());

        // Map the DTO to entity and set the company profile
        Employee employee = EmployeeMapper.toEntity(requestBody);
        employee.setCompany(company);

        // Save the employee profile
        employeeRepository.save(employee);

        // Set response details
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee Profile Created Successfully");
        response.setData(EmployeeMapper.toDTO(employee));
        return response;
    }

    @Override
    public DefaultApiResponse<EmployeeDto> getEmployee(String employeeId) {
        DefaultApiResponse<EmployeeDto> response = new DefaultApiResponse<>();

        // Fetch employee profile or throw exception if not found
        Employee employee = employeeRepository.findByEmployeeId(UUID.fromString(employeeId))
                .orElseThrow(() -> new RuntimeException("Employee Profile Id doesn't exist"));

        // Map the entity to DTO
        EmployeeDto employeeProfileDTO = EmployeeMapper.toDTO(employee);

        // Set response details
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee Profile Retrieved Successfully");
        response.setData(employeeProfileDTO);
        return response;
    }

    @Override
    public DefaultApiResponse<EmployeeDto> updateEmployee(EmployeeRequestDto requestBody, String employeeId) {
        DefaultApiResponse<EmployeeDto> response = new DefaultApiResponse<>();
        Company company = new Company();

        // Fetch the employee profile or throw exception if not found
        Employee employee = employeeRepository.findById(UUID.fromString(employeeId))
                .orElseThrow(() -> new RuntimeException("EmployeeProfileId is invalid"));

        // Verify that the company profile matches
        if (!employee.getCompany().getCompanyId().equals(COMPANY_ID())) {
            throw new RuntimeException("EmployeeProfile doesn't match this CompanyProfileId");
        }

        // Update the destination Employee entity with fields from the source DTO
        Optional<Company> companyProfileOpt = companyRepository.findById(COMPANY_ID());
        if (companyProfileOpt.isPresent()) {
            company = companyProfileOpt.get();
        }

        employee = EmployeeMapper.toEntity(requestBody);
        employee.setCompany(company);

        // Save the updated employee profile
        employeeRepository.save(employee);

        // Set response details with updated employee data
        EmployeeDto responseData = EmployeeMapper.toDTO(employee);
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Updated Employee Profile");
        response.setData(responseData);

        return response;
    }


    @Override
    public DefaultApiResponse<EmployeeDto> deleteEmployee(String employeeId) {
        DefaultApiResponse<EmployeeDto> response = new DefaultApiResponse<>();

        // Fetch the employee profile or throw exception if not found
        Employee employee = employeeRepository.findById(UUID.fromString(employeeId))
                .orElseThrow(() -> new RuntimeException("Invalid EmployeeId"));

        // Verify that the company profile matches
        if (!employee.getCompany().getCompanyId().equals(COMPANY_ID()))
            throw new RuntimeException("Company doesn't match this EmployeeId");

        // Soft Delete the employee profile
        employee.setDeleted(true);
        employeeRepository.save(employee);

        // Set response details
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("EmployeeProfile Successfully Deleted");
        response.setData(
                EmployeeDto.builder()
                        .employeeId(UUID.fromString(employeeId))
                        .firstName(employee.getFirstName())
                        .emailAddress(employee.getEmailAddress())
                        .build()
        );
        return response;
    }

    // Fetch the CompanyProfile by UUID or throw an exception if not found
    private Company verifyAndFetchCompanyUUID(UUID companyId) {
        Optional<Company> companyProfileOpt = companyRepository.findById(companyId);
        if (companyProfileOpt.isEmpty())
            throw new RuntimeException("CompanyId " + companyId + " is invalid");
        return companyProfileOpt.get();
    }

    /* Method to Verify Token Expiration */
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    // Verify if the record already exists by checking the email and phone number within the same company profile
    private void verifyRecord(EmployeeRequestDto requestBody, UUID companyId) {
        log.info("Verifying record of employee profile: checking for existing account");
        if (employeeRepository.existsByEmailAddressAndPhoneNumberAndCompany_CompanyIdAndDeletedIsFalse(
                requestBody.getEmailAddress(),
                requestBody.getPhoneNumber(),
                companyId
        )) {
            throw new RuntimeException("This employee already exists under this COMPANY");
        }
    }
}
