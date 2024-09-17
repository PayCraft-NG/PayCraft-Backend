package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.enums.SalaryCurrency;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Employee;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.Date;
import java.util.UUID;

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
                .bvn(employee.getBvn())
                .bankName(employee.getBankName())
                .accountNumber(employee.getAccountNumber())
                .salaryAmount(employee.getSalaryAmount())
                .salaryCurrency(employee.getSalaryCurrency())
                .build();
    }

    // Convert EmployeeDTO to Employee Entity
    public static Employee toEntity(EmployeeDto employeeDto) {
        if (employeeDto == null) {
            return null;
        }

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

