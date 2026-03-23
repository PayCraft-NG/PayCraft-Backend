package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.dto.EmployerDTO;
import com.aalto.paycraft.dto.EmployerPasswordUpdateDTO;
import com.aalto.paycraft.dto.EmployerUpdateDTO;
import com.aalto.paycraft.service.IEmployerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Employers",
        description = "Employer signup (public) and authenticated profile, password, and account management."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/employer", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployerController {
    private final IEmployerService iEmployerService;

    @Operation(
            summary = "Register employer",
            description = "Public signup — creates a new employer account (no JWT required)."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employer created"),
            @ApiResponse(responseCode = "400", description = "Validation or duplicate email")
    })
    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> createEmployer(
            @Valid @RequestBody EmployerDTO employerDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iEmployerService.createEmployer(employerDTO));
    }

    @Operation(
            summary = "Get current employer",
            description = "Returns the authenticated employer's profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping(value = "/details")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> getEmployer(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.getEmployer());
    }

    @Operation(
            summary = "Update employer profile",
            description = "Update profile fields for the authenticated employer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PutMapping(value = "/update")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> updateEmployer(
            @Valid @RequestBody EmployerUpdateDTO employerUpdateDTO){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.updateEmployer(employerUpdateDTO));
    }

    @Operation(
            summary = "Delete employer",
            description = "Deletes the authenticated employer account per application rules."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @DeleteMapping(value = "/delete")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> deleteEmployer(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.deleteEmployer());
    }

    @Operation(
            summary = "Update password",
            description = "Change password for the authenticated employer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated"),
            @ApiResponse(responseCode = "400", description = "Validation or weak password"),
            @ApiResponse(responseCode = "401", description = "Missing, invalid JWT, or wrong current password")
    })
    @PatchMapping(value = "/update/password")
    public ResponseEntity<DefaultApiResponse<EmployerDTO>> updatePassword(
            @Valid @RequestBody EmployerPasswordUpdateDTO employerPasswordUpdateDTO){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iEmployerService.updateEmployerPassword(employerPasswordUpdateDTO));
    }
}
