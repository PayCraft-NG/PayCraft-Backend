package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.CompanyUpdateDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;

import java.util.List;
public interface ICompanyService {
    DefaultApiResponse<CompanyDTO> createCompany(CompanyDTO companyDTO);
    DefaultApiResponse<CompanyDTO> getCompany();
    DefaultApiResponse<List<CompanyDTO>> getCompaniesByEmployerId(Integer page, Integer pageSize);
    DefaultApiResponse<CompanyDTO> updateCompany(CompanyUpdateDTO companyUpdateDTO);
    DefaultApiResponse<CompanyDTO> deleteCompany();
}
