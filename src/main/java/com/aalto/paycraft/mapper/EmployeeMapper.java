package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;
import com.aalto.paycraft.entity.Employee;

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
}

