package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;

import java.util.UUID;

public interface IEmployerService {
    DefaultApiResponse<EmployerDTO> createEmployer(EmployerDTO employerDTO);
    DefaultApiResponse<EmployerDTO> getEmployer(UUID employerId);
    DefaultApiResponse<EmployerDTO> updateEmployer(EmployerDTO employerDTO,UUID employerId);
    DefaultApiResponse<EmployerDTO> deleteEmployer(UUID employerId);
    DefaultApiResponse<EmployerDTO> updateEmployerPassword(EmployerPasswordUpdateDTO employerPasswordUpdateDTO, UUID employerId);
}
