package com.aalto.paycraft.service.impl;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.repository.EmployerRepository;
import com.aalto.paycraft.service.IEmployerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployerServiceImpl implements IEmployerService {
    private final EmployerRepository employerRepository;

    @Override
    public DefaultApiResponse<EmployerDTO> createEmployer(EmployerDTO employerDTO) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployerDTO> getEmployer(UUID employerId) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployerDTO> updateEmployer(EmployerDTO employerDTO, UUID employerId) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployerDTO> deleteEmployer(UUID employerId) {
        return null;
    }

    @Override
    public DefaultApiResponse<EmployerDTO> updateEmployerPassword(EmployerPasswordUpdateDTO employerPasswordUpdateDTO, UUID employerId) {
        return null;
    }
}
