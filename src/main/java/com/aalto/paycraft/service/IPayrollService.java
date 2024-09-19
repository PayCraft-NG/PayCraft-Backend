package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PayrollDTO;

import java.util.UUID;

public interface IPayrollService {
    DefaultApiResponse<PayrollDTO> createPayroll(PayrollDTO payrollDTO, UUID employerId);
}
