package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.service.ICompanyService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyServiceImpl implements ICompanyService {
    @Override
    public DefaultApiResponse<CompanyDTO> createCompany(CompanyDTO companyDTO, UUID employerId) {
        return null;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> getCompany(UUID companyId) {
        return null;
    }

    @Override
    public DefaultApiResponse<List<CompanyDTO>> getCompaniesByEmployerId(UUID employerId) {
        return null;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> updateCompany(UUID companyId) {
        return null;
    }

    @Override
    public DefaultApiResponse<CompanyDTO> deleteCompany(UUID companyId) {
        return null;
    }
}
