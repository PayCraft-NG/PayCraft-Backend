package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.dto.EmployerUpdateDTO;
import com.aalto.paycraft.service.IEmployerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(
        name = "Employer Controller",
        description = "CRUD REST APIs to CREATE, READ, UPDATE, and DELETE Employer details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/employer", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployerController {
    private final IEmployerService iEmployerService;

    @Operation(
            summary = "Create Employer REST API",
            description = "REST API to create a new employer"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Employer successfully created"
    )
    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> createEmployer(
            @Valid @RequestBody EmployerDTO employerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iEmployerService.createEmployer(employerDTO));
    }

    @Operation(
            summary = "Get Employer REST API",
            description = "REST API to get employer details by employerId"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employer details successfully retrieved"
    )
    @GetMapping(value = "/details")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> getEmployer(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.getEmployer());
    }

    @Operation(
            summary = "Update Employer REST API",
            description = "REST API to update employer details"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employer details successfully updated"
    )
    @PutMapping(value = "/update")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> updateEmployer(
            @Valid @RequestBody EmployerUpdateDTO employerUpdateDTO){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.updateEmployer(employerUpdateDTO));
    }

    @Operation(
            summary = "Delete Employer REST API",
            description = "REST API to delete employer by employerId"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employer successfully deleted"
    )
    @DeleteMapping(value = "/delete")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> deleteEmployer(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.deleteEmployer());
    }

    @Operation(
            summary = "Update Employer Password REST API",
            description = "REST API to update employer password"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Employer password successfully updated"
    )
    @PatchMapping(value = "/update/password")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> updatePassword(
            @Valid @RequestBody EmployerPasswordUpdateDTO employerPasswordUpdateDTO){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.updateEmployerPassword(employerPasswordUpdateDTO));
    }
}
