package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;
import com.aalto.paycraft.entity.Employee;

import java.util.Optional;

public class EmployeeMapper {

    // Convert Employee entity to EmployeeDTO
    public static EmployeeDto toDTO(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeDto.builder()
                .employeeId(employee.getEmployeeId())
                .companyId(employee.getCompany().getCompanyId())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .emailAddress(employee.getEmailAddress())
                .dateOfBirth(employee.getDateOfBirth())
                .streetAddress(employee.getStreetAddress())
                .phoneNumber(employee.getPhoneNumber())
                .jobTitle(employee.getJobTitle())
                .department(employee.getDepartment())
                .bankName(employee.getBankName())
                .bvn(employee.getBvn())
                .accountNumber(employee.getAccountNumber())
                .salaryAmount(employee.getSalaryAmount())
                .salaryCurrency(employee.getSalaryCurrency())
                .build();
    }

    public static Employee toEntity(EmployeeRequestDto employeeDto) {
        if (employeeDto == null) return null;
        Employee employee = new Employee();
        employee.setFirstName(employeeDto.getFirstName());
        employee.setLastName(employeeDto.getLastName());
        employee.setDateOfBirth(employeeDto.getDateOfBirth());
        employee.setEmailAddress(employeeDto.getEmailAddress());
        employee.setPhoneNumber(employeeDto.getPhoneNumber());
        employee.setStreetAddress(employeeDto.getStreetAddress());
        employee.setJobTitle(employeeDto.getJobTitle());
        employee.setDepartment(employeeDto.getDepartment());
        employee.setBvn(employeeDto.getBvn());
        employee.setBankName(employeeDto.getBankName());
        employee.setAccountNumber(employeeDto.getAccountNumber());
        employee.setSalaryAmount(employeeDto.getSalaryAmount());
        employee.setSalaryCurrency(employeeDto.getSalaryCurrency());
        return employee;
    }

    public static void updateEntityFromDto(Employee employee, EmployeeRequestDto employeeDto) {
        if (employeeDto == null || employee == null) {
            return;  // No update if either is null
        }

        // Using Optional to update fields only if they are non-null
        Optional.ofNullable(employeeDto.getFirstName()).ifPresent(employee::setFirstName);
        Optional.ofNullable(employeeDto.getLastName()).ifPresent(employee::setLastName);
        Optional.ofNullable(employeeDto.getDateOfBirth()).ifPresent(employee::setDateOfBirth);
        Optional.ofNullable(employeeDto.getEmailAddress()).ifPresent(employee::setEmailAddress);
        Optional.ofNullable(employeeDto.getPhoneNumber()).ifPresent(employee::setPhoneNumber);
        Optional.ofNullable(employeeDto.getStreetAddress()).ifPresent(employee::setStreetAddress);
        Optional.ofNullable(employeeDto.getJobTitle()).ifPresent(employee::setJobTitle);
        Optional.ofNullable(employeeDto.getDepartment()).ifPresent(employee::setDepartment);
        Optional.ofNullable(employeeDto.getBvn()).ifPresent(employee::setBvn);
        Optional.ofNullable(employeeDto.getBankName()).ifPresent(employee::setBankName);
        Optional.ofNullable(employeeDto.getAccountNumber()).ifPresent(employee::setAccountNumber);
        Optional.ofNullable(employeeDto.getSalaryAmount()).ifPresent(employee::setSalaryAmount);
        Optional.ofNullable(employeeDto.getSalaryCurrency()).ifPresent(employee::setSalaryCurrency);
    }
}

