package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.service.IPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/payroll", produces = MediaType.APPLICATION_JSON_VALUE)
public class PayrollController {
    private final IPayrollService iPayrollService;

    @PostMapping(value = "/create/{employerId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> createPayroll(
            @Valid @RequestBody PayrollDTO payrollDTO,
            @Valid @PathVariable("employerId")UUID employerId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iPayrollService.createPayroll(payrollDTO, employerId));
    }
}
