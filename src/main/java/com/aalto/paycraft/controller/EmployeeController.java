package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployeeDto;
import com.aalto.paycraft.dto.EmployeeRequestDto;
import com.aalto.paycraft.service.IEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Employees",
        description = "Create, list, read, update, and delete employees for the authenticated employer context."
)
@SecurityRequirement(name = "bearerAuth")
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
            summary = "Create employee",
            description = "Create a new employee profile linked to the current company context."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee created"),
            @ApiResponse(responseCode = "400", description = "Validation or business error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
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
            summary = "List all employees",
            description = "Returns all non-deleted employees visible to the authenticated employer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employees retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/all")
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
            summary = "Get employee by ID",
            description = "Fetch a single employee by identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @GetMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> getEmployee(
            @Parameter(description = "Employee identifier (UUID string)", required = true)
            @PathVariable("employeeId") String employeeId) {
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
            summary = "Update employee",
            description = "Update an existing employee profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee updated"),
            @ApiResponse(responseCode = "400", description = "Validation or business error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PutMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> updateEmployee(
            @Valid @RequestBody EmployeeRequestDto requestBody,
            @Parameter(description = "Employee identifier (UUID string)", required = true)
            @PathVariable("employeeId") String employeeId) {
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
            summary = "Delete employee",
            description = "Soft-delete or remove an employee by ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @DeleteMapping("/{employeeId}")
    public ResponseEntity<DefaultApiResponse<EmployeeDto>> deleteEmployeeProfile(
            @Parameter(description = "Employee identifier (UUID string)", required = true)
            @PathVariable("employeeId") String employeeId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(employeeService.deleteEmployee(employeeId));
    }
}
