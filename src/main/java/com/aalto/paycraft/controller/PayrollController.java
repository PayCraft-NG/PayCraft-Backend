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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@Tag(name = "Payroll", description = "Create and manage payrolls, employees on a payroll, and run payroll.")
@SecurityRequirement(name = "bearerAuth")
public class PayrollController {

    private final IPayrollService iPayrollService;

    @Operation(summary = "Create payroll",
            description = "Create a new payroll for the current company context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payroll created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> createPayroll(
            @Valid @RequestBody PayrollDTO payrollDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iPayrollService.createPayroll(payrollDTO));
    }

    @Operation(summary = "Add employee to payroll",
            description = "Attach an employee to an existing payroll.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee added"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll or employee not found")
    })
    @PostMapping(value = "/add-employee/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> addEmployee(
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId,
            @Parameter(description = "Employee UUID to add", required = true) @RequestParam("employeeId") UUID employeeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.addEmployee(payrollId, employeeId));
    }

    @Operation(summary = "Remove employee from payroll",
            description = "Detach an employee from a payroll.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employee removed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll or employee not found")
    })
    @PostMapping(value = "/remove-employee/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> removeEmployee(
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId,
            @Parameter(description = "Employee UUID to remove", required = true) @RequestParam("employeeId") UUID employeeId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.removeEmployee(payrollId, employeeId));
    }

    @Operation(summary = "Update payroll",
            description = "Update payroll metadata or configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @PutMapping(value = "/update/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> updatePayroll(
            @Valid @RequestBody PayrollUpdateDTO payrollUpdateDTO,
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.updatePayroll(payrollUpdateDTO, payrollId));
    }

    @Operation(summary = "Delete payroll",
            description = "Remove a payroll by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @DeleteMapping(value = "/delete/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> deletePayroll(
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.deletePayroll(payrollId));
    }

    @Operation(summary = "List employees on payroll",
            description = "Employees currently assigned to the payroll.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Employees retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @GetMapping(value = "/employees/{payrollId}")
    public ResponseEntity<DefaultApiResponse<List<EmployeeDto>>> getEmployeesByPayrollId(
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getEmployeesByPayrollId(payrollId));
    }

    @Operation(summary = "Get payroll by ID",
            description = "Single payroll detail.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payroll retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @GetMapping(value = "/{payrollId}")
    public ResponseEntity<DefaultApiResponse<PayrollDTO>> getPayroll(
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getPayroll(payrollId));
    }

    @Operation(summary = "List payrolls for company",
            description = "All payrolls visible in the current company context.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payrolls retrieved (may be empty)"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping(value = "/all")
    public ResponseEntity<DefaultApiResponse<List<PayrollDTO>>> getAllPayroll() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.getAllPayroll());
    }

    @Operation(summary = "Run payroll",
            description = "Execute payroll processing for the given payroll id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Run completed or status returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Payroll not found")
    })
    @PostMapping(value = "/run/{payrollId}")
    public ResponseEntity<DefaultApiResponse<?>> runPayroll(
            @Parameter(description = "Payroll UUID") @Valid @PathVariable("payrollId") UUID payrollId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iPayrollService.runPayroll(payrollId));
    }
}
