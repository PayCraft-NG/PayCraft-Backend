package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.SaveEmployeeDto;
import com.aalto.paycraft.services.IEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/v1/employee", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployeeController {

    private final IEmployeeService employeeService;

    /**
     * Endpoint for creating a new employee profile associated with a specific company profile.
     *
     * @param requestBody The DTO object containing employee profile information.
     * @param companyId The unique identifier of the company profile.
     * @return A response entity containing the status of the creation, employee profile data, and the HTTP status code.
     */
    @PostMapping("/create/{companyId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> createEmployee(@Valid @RequestBody SaveEmployeeDto requestBody,
                                                                                 @Valid @PathVariable("companyId") UUID companyId){
        DefaultApiResponse<EmployeeDto> response = employeeService.createEmployeeProfile(requestBody, companyId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for retrieving an employee profile based on the provided employee profile ID.
     *
     * @param employeeId The unique identifier of the employee profile.
     * @return A response entity containing the employee profile data, status message, and the HTTP status code.
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> getEmployee(@Valid @PathVariable("employeeId") UUID employeeId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.getEmployeeProfile(employeeId));
    }

    /**
     * Endpoint for updating an employee profile for a specific company.
     *
     * @param requestBody The DTO object containing the updated employee profile information.
     * @param employeeId The unique identifier of the employee profile to update.
     * @param companyId The unique identifier of the company profile associated with the employee.
     * @return A response entity containing the status of the update, updated employee profile data, and the HTTP status code.
     */
    @PutMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> updateEmployee(@Valid @RequestBody EmployeeDto requestBody,
                                                                                        @Valid @PathVariable("employeeId") UUID employeeId,
                                                                                        @Valid @RequestParam("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.updateEmployeeProfile(requestBody, employeeId, companyId));
    }

    /**
     * Endpoint for deleting an employee profile based on the provided employee profile ID and company profile ID.
     *
     * @param employeeProfileId The unique identifier of the employee profile to delete.
     * @param companyProfileId The unique identifier of the company profile associated with the employee.
     * @return A response entity containing the status of the deletion, a status message, and the HTTP status code.
     */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> deleteEmployeeProfile(
            @Valid @PathVariable("employeeId") UUID employeeProfileId,
            @Valid @RequestParam("companyProfileId") UUID companyProfileId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.deleteEmployeeProfile(employeeProfileId, companyProfileId));
    }
}
