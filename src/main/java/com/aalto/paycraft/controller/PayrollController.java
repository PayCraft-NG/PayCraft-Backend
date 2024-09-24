package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.PayrollDTO;
import com.aalto.paycraft.dto.PayrollUpdateDTO;
import com.aalto.paycraft.service.IPayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/payroll", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payroll Management", description = "Endpoints for managing payroll")
public class PayrollController {

    private final IPayrollService iPayrollService;

    @Operation(summary = "Create a payroll")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payroll created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> createPayroll(
            @Valid @RequestBody PayrollDTO payrollDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iPayrollService.createPayroll(payrollDTO));
    }

    @Operation(summary = "Delete a payroll by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @DeleteMapping(value = "/delete/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> deletePayroll(
            @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.deletePayroll(payrollId));
    }

    @Operation(summary = "Add an employee to a payroll")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee added to payroll"),
            @ApiResponse(responseCode = "404", description = "Payroll or Employee not found")
    })
    @PostMapping(value = "/add-employee/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> addEmployee(
            @Valid @PathVariable("payrollId") UUID payrollId,
            @Parameter(description = "Employee ID to be added") @RequestParam("employeeId") UUID employeeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.addEmployee(payrollId, employeeId));
    }

    @Operation(summary = "Remove an employee from a payroll")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee removed from payroll"),
            @ApiResponse(responseCode = "404", description = "Payroll or Employee not found")
    })
    @PostMapping(value = "/remove-employee/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> removeEmployee(
            @Valid @PathVariable("payrollId") UUID payrollId,
            @Parameter(description = "Employee ID to be removed") @RequestParam("employeeId") UUID employeeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.removeEmployee(payrollId, employeeId));
    }

    @Operation(summary = "Get all employees by payroll ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of employees retrieved"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @GetMapping(value = "/employees/{payrollId}")
    public ResponseEntity<DefaultApiResponse<List<EmployeeDto>>> getEmployeesByPayrollId(
            @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getEmployeesByPayrollId(payrollId));
    }

    @Operation(summary = "Get payroll by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll retrieved"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @GetMapping(value = "/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> getPayroll(
            @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getPayroll(payrollId));
    }

    @Operation(summary = "Update a payroll by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @PutMapping(value = "/update/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> updatePayroll(
            @Valid @RequestBody PayrollUpdateDTO payrollUpdateDTO,
            @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.updatePayroll(payrollUpdateDTO, payrollId));
    }
}
