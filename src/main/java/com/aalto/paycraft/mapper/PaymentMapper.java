package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.PaymentDTO;
import com.aalto.paycraft.entity.Payment;

import java.util.Optional;

public class PaymentMapper {

    // Convert Payment entity to PaymentDTO
    public static PaymentDTO toDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        return PaymentDTO.builder()
                .referenceNumber(payment.getReferenceNumber())
                .amount(payment.getAmount())
                .transactionType(payment.getTransactionType())
                .transactionDateTime(payment.getTransactionDateTime())
                .description(payment.getDescription())
                .currency(payment.getCurrency())
                .payrollName(payment.getPayrollName())
                .employeeName(payment.getEmployeeName())
                .build();
    }

    // Convert PaymentDTO to Payment entity
    public static Payment toEntity(PaymentDTO paymentDTO) {
        if (paymentDTO == null) return null;

        Payment payment = new Payment();
        payment.setReferenceNumber(paymentDTO.getReferenceNumber());
        payment.setAmount(paymentDTO.getAmount());
        payment.setTransactionType(paymentDTO.getTransactionType());
        payment.setTransactionDateTime(paymentDTO.getTransactionDateTime());
        payment.setDescription(paymentDTO.getDescription());
        payment.setCurrency(paymentDTO.getCurrency());
        payment.setPayrollName(paymentDTO.getPayrollName());
        payment.setEmployeeName(paymentDTO.getEmployeeName());

        return payment;
    }

    // Update Payment entity from PaymentDTO
    public static void updateEntityFromDto(Payment payment, PaymentDTO paymentDTO) {
        if (paymentDTO == null || payment == null) {
            return; // No update if either is null
        }

        // Using Optional to update fields only if they are non-null
        Optional.ofNullable(paymentDTO.getReferenceNumber()).ifPresent(payment::setReferenceNumber);
        Optional.ofNullable(paymentDTO.getAmount()).ifPresent(payment::setAmount);
        Optional.ofNullable(paymentDTO.getTransactionType()).ifPresent(payment::setTransactionType);
        Optional.ofNullable(paymentDTO.getTransactionDateTime()).ifPresent(payment::setTransactionDateTime);
        Optional.ofNullable(paymentDTO.getDescription()).ifPresent(payment::setDescription);
        Optional.ofNullable(paymentDTO.getCurrency()).ifPresent(payment::setCurrency);
        Optional.ofNullable(paymentDTO.getPayrollName()).ifPresent(payment::setPayrollName);
        Optional.ofNullable(paymentDTO.getEmployeeName()).ifPresent(payment::setEmployeeName);
    }
}
