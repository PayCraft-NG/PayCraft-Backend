package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.dto.EmployerUpdateDTO;

import java.util.UUID;

public interface IEmployerService {
    DefaultApiResponse<EmployerDTO> createEmployer(EmployerDTO employerDTO);
    DefaultApiResponse<EmployerDTO> getEmployer();
    DefaultApiResponse<EmployerDTO> updateEmployer(EmployerUpdateDTO employerUpdateDTO);
    DefaultApiResponse<EmployerDTO> deleteEmployer();
    DefaultApiResponse<EmployerDTO> updateEmployerPassword(EmployerPasswordUpdateDTO employerPasswordUpdateDTO);
}
