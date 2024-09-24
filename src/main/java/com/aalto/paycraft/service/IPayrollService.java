package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.dto.PayrollUpdateDTO;

import java.util.List;
import java.util.UUID;

public interface IPayrollService {
    DefaultApiResponse<PayrollDTO> createPayroll(PayrollDTO payrollDTO);
    DefaultApiResponse<PayrollDTO> deletePayroll(UUID payrollId);
    DefaultApiResponse<PayrollDTO> addEmployee(UUID payrollId, UUID employeeId);
    DefaultApiResponse<PayrollDTO> removeEmployee(UUID payrollId, UUID employeeId);
    DefaultApiResponse<List<EmployeeDto>> getEmployeesByPayrollId(UUID payrollId);
    DefaultApiResponse<PayrollDTO> getPayroll(UUID payrollId);
    DefaultApiResponse<PayrollDTO> updatePayroll(PayrollUpdateDTO payrollUpdateDTO, UUID payroll);
}
