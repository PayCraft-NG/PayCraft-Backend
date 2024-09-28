package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.*;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employee;
import com.aalto.paycraft.entity.Payroll;
import com.aalto.paycraft.mapper.CompanyMapper;
import com.aalto.paycraft.mapper.PayrollMapper;
import com.aalto.paycraft.repository.CompanyRepository;
import com.aalto.paycraft.repository.EmployeeRepository;
import com.aalto.paycraft.repository.PayrollRepository;
import com.aalto.paycraft.service.IPayrollService;
import com.aalto.paycraft.service.JWTService;
import com.aalto.paycraft.service.PayrollJobService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements IPayrollService {
    private final PayrollRepository payrollRepository;
    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;
    private final JWTService jwtService;
    private final HttpServletRequest request;
    private final PayrollJobService payrollJobService;

    // Gets the AccessToken from the Request Sent
    private String ACCESS_TOKEN() {
        return request.getHeader("Authorization").substring(7); // Removes "Bearer " prefix
    }

    // Get the ID of the company making the request
    private UUID COMPANY_ID() {
        verifyTokenExpiration(ACCESS_TOKEN());
        Claims claims = jwtService.extractClaims(ACCESS_TOKEN(), Function.identity());
        return UUID.fromString((String) claims.get("activeCompanyID"));
    }

    // ====== CREATE ======
    @Override
    public DefaultApiResponse<PayrollDTO> createPayroll(PayrollDTO payrollDTO) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();

        // Check if payroll is automatic and set last run date if true
        if (payrollDTO.getAutomatic() != null && payrollDTO.getAutomatic()) {
            payrollDTO.setLastRunDate(LocalDate.now()); // Set last run date as now
            verifyCronExpression(payrollDTO.getCronExpression());
        }

        log.info("Company ID from token: {}", COMPANY_ID());

        Company company = verifyAndFetchCompanyById(COMPANY_ID());
        payrollDTO.setCompanyDTO(CompanyMapper.toDTO(company)); // Map company to DTO

        Payroll payroll = PayrollMapper.toEntity(payrollDTO);
        payroll.setCompany(company); // Set the company for the payroll

        Payroll savedPayroll = payrollRepository.save(payroll); // Save payroll

        // Schedule payroll if it's automatic and has a valid cron expression
        if (savedPayroll.getCronExpression() != null && savedPayroll.getAutomatic() && !savedPayroll.getCronExpression().isEmpty())
            payrollJobService.schedulePayroll(savedPayroll);

        // Set response details
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

    // ====== ADD ======
    @Override
    public DefaultApiResponse<PayrollDTO> addEmployee(UUID payrollId, UUID employeeId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();

        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        Employee employee = verifyAndFetchEmployeeById(employeeId);

        // Check if employee is already on payroll
        if (payroll.getEmployees().contains(employee))
            throw new RuntimeException("Employee with ID " + employee.getEmployeeId() + " already on payroll: " + payroll.getPayrollId());

        payroll.getEmployees().add(employee); // Add employee to payroll
        payrollRepository.save(payroll); // Save updated payroll

        List<UUID> employees = payroll.getEmployees().stream()
                .map(Employee::getEmployeeId)
                .toList(); // Get updated employee list

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee added to payroll");
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .employees(employees)
                        .build()
        );
        return response;
    }

    // ====== REMOVE ======
    @Override
    public DefaultApiResponse<PayrollDTO> removeEmployee(UUID payrollId, UUID employeeId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();

        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        Employee employee = verifyAndFetchEmployeeById(employeeId);

        // Check if employee is not on payroll before removal
        if (!payroll.getEmployees().remove(employee))
            throw new RuntimeException("Employee not on payroll: " + payroll.getPayrollId());

        payrollRepository.save(payroll); // Save updated payroll

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Employee removed from payroll");
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .employees(payroll.getEmployees().stream()
                                .map(Employee::getEmployeeId)
                                .toList())
                        .build()
        );
        return response;
    }

    // ====== UPDATE ======
    @Override
    public DefaultApiResponse<PayrollDTO> updatePayroll(PayrollUpdateDTO payrollUpdateDTO, UUID payrollId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);

        // Verify cron expression if payroll is automatic
        if (payrollUpdateDTO.getAutomatic() != null && payrollUpdateDTO.getAutomatic()) {
            verifyCronExpression(payrollUpdateDTO.getCronExpression());
        }

        Payroll savedPayroll = payrollRepository.save(updatePayrollRecord(payroll, payrollUpdateDTO));

        // Update the scheduled payrolls if it is automatic
        if (savedPayroll.getCronExpression() != null && savedPayroll.getAutomatic() && !savedPayroll.getCronExpression().isEmpty())
            payrollJobService.schedulePayroll(savedPayroll);
        else
            payrollJobService.cancelScheduledPayroll(savedPayroll.getPayrollId()); // Remove payroll if no longer automatic

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Payroll updated successfully");
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .automatic(payrollUpdateDTO.getAutomatic())
                        .cronExpression(payrollUpdateDTO.getCronExpression())
                        .build()
        );
        return response;
    }

    // ====== DELETE ======
    @Override
    public DefaultApiResponse<PayrollDTO> deletePayroll(UUID payrollId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Company company = verifyAndFetchCompanyById(COMPANY_ID());
        Payroll payroll = verifyAndFetchPayrollById(payrollId);

        payrollRepository.delete(payroll); // Delete the payroll

        // Remove payroll from scheduled jobs if it's automatic
        if (payroll.getCronExpression() != null && payroll.getAutomatic() && !payroll.getCronExpression().isEmpty())
            payrollJobService.cancelScheduledPayroll(payroll.getPayrollId());

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

    // ====== GET EMPLOYEES BY PAYROLL ID ======
    @Override
    public DefaultApiResponse<List<EmployeeDto>> getEmployeesByPayrollId(UUID payrollId) {
        DefaultApiResponse<List<EmployeeDto>> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);

        List<UUID> employeeList = payroll.getEmployees().stream()
                .map(Employee::getEmployeeId)
                .toList(); // Get employee IDs from payroll

        // Fetch all employees in one query for optimization
        List<Employee> employees = employeeRepository.findAllById(employeeList);
        List<EmployeeDto> responseList = employees.stream()
                .map(employee -> EmployeeDto.builder()
                        .employeeId(employee.getEmployeeId())
                        .firstName(employee.getFirstName())
                        .lastName(employee.getLastName())
                        .emailAddress(employee.getEmailAddress())
                        .phoneNumber(employee.getPhoneNumber())
                        .accountNumber(employee.getAccountNumber())
                        .bankName(employee.getBankName())
                        .streetAddress(employee.getStreetAddress())
                        .build())
                .toList();

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("List of Employees by PayrollId: " + payrollId);
        response.setData(responseList);
        return response;
    }

    // ====== GET PAYROLL ======
    @Override
    public DefaultApiResponse<PayrollDTO> getPayroll(UUID payrollId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Payroll payroll = verifyAndFetchPayrollById(payrollId);
        PayrollDTO payrollDTO = PayrollMapper.toDto(payroll);
        payrollDTO.setPayrollId(payroll.getPayrollId());

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("Payroll details");
        response.setData(payrollDTO);
        return response;
    }

    @Override
    public DefaultApiResponse<List<PayrollDTO>> getAllPayroll() {
        DefaultApiResponse<List<PayrollDTO>> response = new DefaultApiResponse<>();

        List<Payroll> payrollList = payrollRepository.findAllByCompanyId(COMPANY_ID());
        List<PayrollDTO> payrollDTOList =
                payrollList.stream().map(PayrollMapper::toDto)
                        .toList();

        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setStatusMessage("All Payroll details");
        response.setData(payrollDTOList);

        return response;
    }

    // Fetch company by ID and verify existence
    private Company verifyAndFetchCompanyById(UUID companyId) {
        return companyRepository.findById(companyId).orElseThrow(
                () -> new RuntimeException("Company ID does not exist: " + companyId)
        );
    }

    // Fetch payroll by ID and verify existence
    private Payroll verifyAndFetchPayrollById(UUID payrollId) {
        return payrollRepository.findById(payrollId).orElseThrow(
                () -> new RuntimeException("Payroll ID does not exist: " + payrollId)
        );
    }

    // Fetch employee by ID and verify existence
    private Employee verifyAndFetchEmployeeById(UUID employeeId) {
        return employeeRepository.findByEmployeeId(employeeId).orElseThrow(
                () -> new RuntimeException("Employee ID does not exist: " + employeeId)
        );
    }

    // Update the payroll record with new data
    private Payroll updatePayrollRecord(Payroll payroll, PayrollUpdateDTO payrollUpdateDTO) {
        if (payrollUpdateDTO.getAutomatic() != null)
            payroll.setAutomatic(payrollUpdateDTO.getAutomatic());
        if (payrollUpdateDTO.getCronExpression() != null)
            payroll.setCronExpression(payrollUpdateDTO.getCronExpression());
        return payroll;
    }

    // Verify token expiration
    private void verifyTokenExpiration(String token) {
        if (jwtService.isTokenExpired(token)) {
            log.warn("Token has expired");
            throw new ExpiredJwtException(null, null, "Access Token has expired");
        }
    }

    // Verify if the provided cron expression is valid
    private void verifyCronExpression(String cronExpression) {
        try {
            CronTrigger cronTrigger = new CronTrigger(cronExpression); // Throws exception if invalid
        } catch (Exception e) {
            throw new RuntimeException("Invalid Cron Expression " + cronExpression);
        }
    }
}
