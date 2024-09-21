package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employee;
import com.aalto.paycraft.entity.Payroll;
import com.aalto.paycraft.mapper.CompanyMapper;
import com.aalto.paycraft.mapper.EmployeeMapper;
import com.aalto.paycraft.mapper.EmployerMapper;
import com.aalto.paycraft.mapper.PayrollMapper;
import com.aalto.paycraft.repository.CompanyRepository;
import com.aalto.paycraft.repository.EmployeeRepository;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.repository.PayrollRepository;
import com.aalto.paycraft.service.IPayrollService;
import com.aalto.paycraft.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements IPayrollService {
    private static final Logger log = LoggerFactory.getLogger(PayrollServiceImpl.class);
    private final PayrollRepository payrollRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final JWTService jwtService;
    private final HttpServletRequest request;

    // Gets the AccessToken from the Request Sent
    private String ACCESS_TOKEN(){
        return request.getHeader("Authorization").substring(7);
    }

    // Get the ID of the employer making the request
    private UUID EMPLOYER_ID(){
        verifyTokenExpiration(ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(ACCESS_TOKEN(), Function.identity());  // Function.identity() returns the same object
        return UUID.fromString((String) claims.get("userID"));
    }

    // Get the ID of the company making the request
    private UUID COMPANY_ID(){
        verifyTokenExpiration(ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(ACCESS_TOKEN(), Function.identity());  // Function.identity() returns the same object
        return UUID.fromString((String) claims.get("activeCompanyID"));
    }

    @Override
    public DefaultApiResponse<PayrollDTO> createPayroll(PayrollDTO payrollDTO, UUID companyId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();

        // Check if payroll is automatic
        if (payrollDTO.getAutomatic() != null && payrollDTO.getAutomatic())
            payrollDTO.setLastRunDate(LocalDate.now()); // Set runDate as now (this would be the last run date)

        log.info("companyID from token: {}", COMPANY_ID());
        log.info("companyID from path: {}", COMPANY_ID());

        Company company = verifyAndFetchCompanyById(companyId);
        payrollDTO.setCompanyDTO(CompanyMapper.toDTO(company));

        Payroll payroll = PayrollMapper.toEntity(payrollDTO);
        payrollRepository.save(payroll);

        response.setStatusMessage("Payroll created successfully");
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .lastRunDate(payroll.getLastRunDate())
                        .companyDTO(
                                CompanyDTO.builder()
                                        .companyId(company.getCompanyId())
                                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                                        .companyEmailAddress(company.getCompanyEmailAddress())
                                        .build()
                        )
                        .build());
        return response;
    }

    @Override
    public DefaultApiResponse<PayrollDTO> deletePayroll(UUID payrollId, UUID companyId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Company company = verifyAndFetchCompanyById(companyId);
        Payroll payroll = verifyAndFetchPayrollById(payrollId);

        payrollRepository.delete(payroll);

        response.setStatusMessage("Payroll deleted successfully");
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .companyDTO(
                                CompanyDTO.builder()
                                        .companyId(company.getCompanyId())
                                        .companyEmailAddress(company.getCompanyEmailAddress())
                                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                                        .build())
                        .build()
        );
        return response;
    }

    @Override
    public DefaultApiResponse<PayrollDTO> addEmployee(UUID payrollId, UUID employeeId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        Employee employee = verifyAndFetchEmployeeById(employeeId);

        List<Employee> employeeList = payroll.getEmployees();

        if (employeeList.contains(employee))
            throw new RuntimeException("Employee already on payroll: " + payroll.getPayrollId());

        employeeList.add(employee);
        payroll.setEmployees(employeeList);

        payrollRepository.save(payroll);

        List<EmployeeDto> employeeDtoList = employeeList.stream().map(
                EmployeeMapper::toDTO
        ).toList();

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee added to payroll");
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .employees(employeeDtoList)
                        .build()
        );
        return response;
    }


    @Override
    public DefaultApiResponse<PayrollDTO> removeEmployee(UUID payrollId, UUID employeeId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        Employee employee = verifyAndFetchEmployeeById(employeeId);

        List<Employee> employeeList = payroll.getEmployees();
        if(!employeeList.remove(employee))
            throw  new RuntimeException("Employee not on payroll: " + payroll.getPayrollId());
        payroll.setEmployees(employeeList);
        payrollRepository.save(payroll);

        List<EmployeeDto> employeeDtoList = employeeList.stream().map(
                EmployeeMapper::toDTO
        ).toList();

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee removed to payroll");
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .employees(employeeDtoList)
                        .build()
        );
        return response;
    }

    @Override
    public DefaultApiResponse<List<EmployeeDto>> getEmployeesByPayrollId(UUID payrollId) {
        DefaultApiResponse<List<EmployeeDto>> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);

        List<Employee> employeeList = payroll.getEmployees();
        List<EmployeeDto> responseList = employeeList.stream()
                .map(employee ->
                        EmployeeDto.builder()
                                .employeeId(employee.getEmployeeId())
                                .firstName(employee.getFirstName())
                                .lastName(employee.getLastName())
                                .emailAddress(employee.getEmailAddress())
                                .phoneNumber(employee.getPhoneNumber())
                                .accountNumber(employee.getAccountNumber())
                                .bankName(employee.getBankName())
                                .emailAddress(employee.getEmailAddress())
                                .streetAddress(employee.getStreetAddress())
                                .build()
                ).toList();
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("List of Employees by PayrollId: " + payrollId);
        response.setData(responseList);
        return response;
    }

    @Override
    public DefaultApiResponse<PayrollDTO> getPayroll(UUID payrollId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Payroll details");
        response.setData(PayrollMapper.toDto(payroll));
        return response;
    }

    @Override
    public DefaultApiResponse<PayrollDTO> updatePayroll(PayrollUpdateDTO payrollUpdateDTO, UUID payrollId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        payrollRepository.save(updatePayrollRecord(payroll,payrollUpdateDTO));
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Payroll updated successfully");
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .automatic(payrollUpdateDTO.getAutomatic())
                        .frequency(payrollUpdateDTO.getFrequency())
                        .build()
        );
        return null;
    }

    private Company verifyAndFetchCompanyById(UUID companyId){
        return companyRepository.findById(companyId).orElseThrow(
                ()-> new RuntimeException("Company ID does not exist: " + companyId)
        );
    }

    private Payroll verifyAndFetchPayrollById(UUID payrollId){
        return payrollRepository.findById(payrollId).orElseThrow(
                () -> new RuntimeException("Payroll ID does not exist: " + payrollId)
        );
    }

    private Employee verifyAndFetchEmployeeById(UUID employeeId){
        return employeeRepository.findById(employeeId).orElseThrow(
                () -> new RuntimeException("Employee ID does not exist: " + employeeId)
        );
    }

    private Payroll updatePayrollRecord(Payroll payroll, PayrollUpdateDTO payrollUpdateDTO){
        if(payrollUpdateDTO.getAutomatic() != null)
            payroll.setAutomatic(payrollUpdateDTO.getAutomatic());
        if(payrollUpdateDTO.getFrequency() != null)
            payroll.setFrequency(payrollUpdateDTO.getFrequency());
        return payroll;
    }

    /* Method to Verify Token Expiration */
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }
}

