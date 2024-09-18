package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.SaveEmployeeDto;
import com.aalto.paycraft.service.IEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service @RequiredArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    @Override
    public DefaultApiResponse<EmployeeDto> createEmployeeProfile(SaveEmployeeDto requestBody, UUID companyId) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployeeDto> getEmployeeProfile(UUID employeeId) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployeeDto> updateEmployeeProfile(EmployeeDto requestBody, UUID employeeId, UUID companyId) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployeeDto> deleteEmployeeProfile(UUID employeeId, UUID companyId) {
        return null;
    }
}
