package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.service.IEmployerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/employer", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployerController {
    private final IEmployerService iEmployerService;

    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> createEmployer(
            @Valid @RequestBody EmployerDTO employerDTO) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.createEmployer(employerDTO));
    }

    @GetMapping(value = "/{employerId}")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> getEmployer(
            @Valid @PathVariable("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.getEmployer(employerId));
    }

    @PutMapping(value = "/update/{employerId}")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> updateEmployer(
            @Valid @RequestBody EmployerDTO employerDTO,
            @Valid @PathVariable("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.updateEmployer(employerDTO, employerId));
    }

    @DeleteMapping(value = "/delete/{employerId}")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> deleteEmployer(
            @Valid @PathVariable("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.deleteEmployer(employerId));
    }

    @PatchMapping(value = "/update/password/{employerId}")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> updatePassword(
            @Valid @RequestBody EmployerPasswordUpdateDTO employerPasswordUpdateDTO,
            @Valid @PathVariable("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.updateEmployerPassword(employerPasswordUpdateDTO, employerId));
    }
}
