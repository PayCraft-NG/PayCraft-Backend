package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;

import java.util.List;
import java.util.UUID;

public interface IEmployeeService {
    DefaultApiResponse<EmployeeDto> createEmployee(EmployeeRequestDto requestBody);
    DefaultApiResponse<List<EmployeeDto>> getAllEmployees();
    DefaultApiResponse<EmployeeDto> getEmployee(String employeeId);
    DefaultApiResponse<EmployeeDto> updateEmployee(EmployeeRequestDto requestBody, String employeeId);
    DefaultApiResponse<EmployeeDto> deleteEmployee(String employeeId);
}
