package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;
import com.aalto.paycraft.service.IEmployeeService;
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
     * @return A response entity containing the status of the creation, employee profile data, and the HTTP status code.
     */
    @PostMapping("/create")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> createEmployee(@Valid @RequestBody EmployeeRequestDto requestBody){
        DefaultApiResponse<EmployeeDto> response = employeeService.createEmployee(requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for retrieving an employee profile based on the provided employee profile ID.
     *
     * @param employeeId The unique identifier of the employee profile.
     * @return A response entity containing the employee profile data, status message, and the HTTP status code.
     */
    @GetMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> getEmployee(@Valid @PathVariable("employeeId") String employeeId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.getEmployee(employeeId));
    }

    /**
     * Endpoint for updating an employee profile for a specific company.
     *
     * @param requestBody The DTO object containing the updated employee profile information.
     * @param employeeId The unique identifier of the employee profile to update.
     * @return A response entity containing the status of the update, updated employee profile data, and the HTTP status code.
     */
    @PutMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> updateEmployee(@Valid @RequestBody EmployeeRequestDto requestBody,
                                                                          @Valid @PathVariable("employeeId") String employeeId ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.updateEmployee(requestBody, employeeId ));
    }

    /**
     * Endpoint for deleting an employee profile based on the provided employee profile ID and company profile ID.
     *
     * @param employeeId The unique identifier of the employee profile to delete.
     * @return A response entity containing the status of the deletion, a status message, and the HTTP status code.
     */
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> deleteEmployeeProfile(
            @Valid @PathVariable("employeeId") String employeeId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.deleteEmployee(employeeId));
    }
}
