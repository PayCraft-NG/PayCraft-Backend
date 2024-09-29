package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PaymentDTO;

import java.util.UUID;

public interface IPaymentService {
    DefaultApiResponse<PaymentDTO> payEmployee(UUID employeeId);
}
