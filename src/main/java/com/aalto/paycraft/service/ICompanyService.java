package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;

import java.util.List;
import java.util.UUID;

public interface ICompanyService {
    DefaultApiResponse<CompanyDTO> createCompany(CompanyDTO companyDTO, UUID employerId);
    DefaultApiResponse<CompanyDTO> getCompany(UUID companyId);
    DefaultApiResponse<List<CompanyDTO>> getCompaniesByEmployerId(UUID employerId);
    DefaultApiResponse<CompanyDTO> updateCompany(UUID companyId);
    DefaultApiResponse<CompanyDTO> deleteCompany(UUID companyId);
}
