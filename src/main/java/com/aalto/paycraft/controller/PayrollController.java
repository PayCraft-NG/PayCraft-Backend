package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.service.IPayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
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

    @PostMapping(value = "/create/{companyId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> createPayroll(
            @Valid @RequestBody PayrollDTO payrollDTO,
            @Valid @PathVariable("companyId")UUID companyId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iPayrollService.createPayroll(payrollDTO, companyId));
    }

    @DeleteMapping(value = "/delete/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> deletePayroll(
            @Valid @PathVariable("payrollId") UUID payrollId,
            @Valid @RequestParam("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.deletePayroll(payrollId, companyId));
    }

    @PostMapping(value = "/add-employee/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> addEmployee(
            @Valid @PathVariable("payrollId") UUID payrollId,
            @Valid @RequestParam("employeeId") UUID employeeId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.addEmployee(payrollId,employeeId));
    }

    @PostMapping(value = "/remove-employee/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> removeEmployee(
            @Valid @PathVariable("payrollId") UUID payrollId,
            @Valid @RequestParam("employeeId") UUID employeeId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.removeEmployee(payrollId,employeeId));
    }

    @GetMapping(value = "/employees/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> getEmployeeByPayrollId(
            @Valid @PathVariable("payrollId") UUID payrollId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getEmployeeByPayrollId(payrollId));
    }

    @GetMapping(value = "/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> getPayroll(
            @Valid @PathVariable("payrollId") UUID payrollId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getPayroll(payrollId));
    }

    @PutMapping(value = "/update/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> updatePayroll(
            @Valid
            @Valid @PathVariable("payrollId") UUID payroll
    )
    )
}
