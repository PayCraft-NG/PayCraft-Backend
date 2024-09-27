package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;
import com.aalto.paycraft.service.IEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Employee Controller",
        description = "CRUD REST APIs to CREATE, READ, UPDATE, and DELETE Employee details"
)
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
    @Operation(
            summary = "Create Employee REST API",
            description = "REST API to create a new employer"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Employee successfully created"
    )
    @PostMapping("/create")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> createEmployee(@Valid @RequestBody EmployeeRequestDto requestBody){
        DefaultApiResponse<EmployeeDto> response = employeeService.createEmployee(requestBody);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Endpoint for retrieving a list of all non-deleted employee profiles.
     *
     * @return A response entity containing the list of employee profiles, status message, and the HTTP status code.
     */
    @Operation(
            summary = "Get All Employees REST API",
            description = "REST API to get all non-deleted employee profiles"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee profiles successfully retrieved"
    )
    @GetMapping // This will handle GET requests to /employees
    public ResponseEntity<DefaultApiResponse<List<EmployeeDto>>> getEmployees() {
        DefaultApiResponse<List<EmployeeDto>> response = employeeService.getAllEmployees(); // Call the service method
        return ResponseEntity.status(HttpStatus.OK).body(response); // Return the response with OK status
    }

    /**
     * Endpoint for retrieving an employee profile based on the provided employee profile ID.
     *
     * @param employeeId The unique identifier of the employee profile.
     * @return A response entity containing the employee profile data, status message, and the HTTP status code.
     */
    @Operation(
            summary = "Get Employee REST API",
            description = "REST API to get employer details"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee details successfully retrieved"
    )
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
    @Operation(
            summary = "Update Employee REST API",
            description = "REST API to update employee details"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee details successfully updated"
    )
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
    @Operation(
            summary = "Delete Employee REST API",
            description = "REST API to delete employee"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employee successfully deleted"
    )
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> deleteEmployeeProfile(
            @Valid @PathVariable("employeeId") String employeeId
    ){
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.deleteEmployee(employeeId));
    }
}
