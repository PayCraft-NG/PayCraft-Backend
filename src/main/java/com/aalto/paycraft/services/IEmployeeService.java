package com.aalto.paycraft.services;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.SaveEmployeeDto;

import java.util.UUID;

public interface IEmployeeService {
    DefaultApiResponse<EmployeeDto> createEmployeeProfile(SaveEmployeeDto requestBody, UUID companyId);
    DefaultApiResponse<EmployeeDto> getEmployeeProfile(UUID employeeId);
    DefaultApiResponse<EmployeeDto> updateEmployeeProfile(EmployeeDto requestBody, UUID employeeId, UUID companyId);
    DefaultApiResponse<EmployeeDto> deleteEmployeeProfile(UUID employeeId, UUID companyId);
}
