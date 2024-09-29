package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PaymentDTO;

import java.util.List;
import java.util.UUID;

public interface IPaymentService {
    DefaultApiResponse<List<String>> getBankNames() throws Exception;
    DefaultApiResponse<?> payEmployee(UUID employeeId);
    DefaultApiResponse<?> verifyPayment(String referenceNumber);
}
