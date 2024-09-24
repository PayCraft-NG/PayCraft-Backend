package com.aalto.paycraft.mapper;

import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.dto.enums.PaymentStatus;
import com.aalto.paycraft.entity.Payroll;


public class PayrollMapper{
    public static Payroll toEntity(PayrollDTO payrollDTO) {
        if (payrollDTO == null) {
            return null;
        }
        return Payroll.builder()
                .automatic(payrollDTO.getAutomatic())
                .payPeriodStart(payrollDTO.getPayPeriodStart())
                .payPeriodEnd(payrollDTO.getPayPeriodEnd())
                .lastRunDate(payrollDTO.getLastRunDate())
                .cronExpression(payrollDTO.getCronExpression())
                .paymentStatus(PaymentStatus.PENDING)
                .build();
    }

    public static PayrollDTO toDto(Payroll payroll) {
        if (payroll == null) {
            return null;
        }
        return PayrollDTO.builder()
                .automatic(payroll.getAutomatic())
                .payPeriodStart(payroll.getPayPeriodStart())
                .payPeriodEnd(payroll.getPayPeriodEnd())
                .lastRunDate(payroll.getLastRunDate())
                .cronExpression(payroll.getCronExpression())
                .paymentStatus(payroll.getPaymentStatus())
                .build();
    }
}
