package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.CompanyUpdateDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;

import java.util.List;
import java.util.UUID;

public interface ICompanyService {
    DefaultApiResponse<CompanyDTO> createCompany(CompanyDTO companyDTO, UUID employerId);
    DefaultApiResponse<CompanyDTO> getCompany();
    DefaultApiResponse<List<CompanyDTO>> getCompaniesByEmployerId(Integer page, Integer pageSize);
    DefaultApiResponse<CompanyDTO> updateCompany(CompanyUpdateDTO companyUpdateDTO);
    DefaultApiResponse<CompanyDTO> deleteCompany();
}
