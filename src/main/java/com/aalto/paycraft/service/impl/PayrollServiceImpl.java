package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.constants.PayCraftConstant;
import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.entity.Company;
import com.aalto.paycraft.entity.Payroll;
import com.aalto.paycraft.mapper.CompanyMapper;
import com.aalto.paycraft.mapper.PayrollMapper;
import com.aalto.paycraft.repository.CompanyRepository;
import com.aalto.paycraft.repository.PayrollRepository;
import com.aalto.paycraft.service.IPayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayrollServiceImpl implements IPayrollService {
    private final PayrollRepository payrollRepository;
    private final CompanyRepository companyRepository;

    @Override
    public DefaultApiResponse<PayrollDTO> createPayroll(PayrollDTO payrollDTO, UUID companyId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();

        // Check if payroll is automatic
        if (payrollDTO.getAutomatic() != null && payrollDTO.getAutomatic())
            payrollDTO.setLastRunDate(LocalDate.now()); // Set runDate as now (this would be the last run date)

        Company company = verifyAndFetchCompanyById(companyId);
        payrollDTO.setCompanyDTO(CompanyMapper.toDTO(company));

        Payroll payroll = PayrollMapper.toEntity(payrollDTO);
        payrollRepository.save(payroll);

        response.setStatusMessage("Payroll created successfully");
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .lastRunDate(payroll.getLastRunDate())
                        .companyDTO(
                                CompanyDTO.builder()
                                        .companyId(company.getCompanyId())
                                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                                        .companyEmailAddress(company.getCompanyEmailAddress())
                                        .build()
                        )
                        .build());
        return response;
    }

    @Override
    public DefaultApiResponse<PayrollDTO> deletePayroll(UUID payrollId, UUID companyId) {
        DefaultApiResponse<PayrollDTO> response = new DefaultApiResponse<>();
        Company company = verifyAndFetchCompanyById(companyId);
        Payroll payroll = verifyAndFetchPayrollById(payrollId);

        payrollRepository.delete(payroll);

        response.setStatusMessage("Payroll deleted successfully");
        response.setStatusCode(PayCraftConstant.REQUEST_SUCCESS);
        response.setData(
                PayrollDTO.builder()
                        .payrollId(payroll.getPayrollId())
                        .companyDTO(
                                CompanyDTO.builder()
                                        .companyId(company.getCompanyId())
                                        .companyEmailAddress(company.getCompanyEmailAddress())
                                        .companyPhoneNumber(company.getCompanyPhoneNumber())
                                        .build())
                        .build()
        );
        return response;
    }

    private Company verifyAndFetchCompanyById(UUID companyId){
        return companyRepository.findById(companyId).orElseThrow(
                ()-> new RuntimeException("Company ID does not exist: " + companyId)
        );
    }

    private Payroll verifyAndFetchPayrollById(UUID payrollId){
        return payrollRepository.findById(payrollId).orElseThrow(
                () -> new RuntimeException("Payroll ID does not exist: " + payrollId)
        );
    }
}

