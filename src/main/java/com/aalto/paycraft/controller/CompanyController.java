package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.CompanyUpdateDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.service.ICompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Companies",
        description = "Company CRUD for employers. Creating a company is allowed without JWT when employerId is supplied."
)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/company", produces = MediaType.APPLICATION_JSON_VALUE)
public class CompanyController {
    private final ICompanyService iCompanyService;

    @Operation(
            summary = "Create company",
            description = "Public when called with a valid employerId (post-signup onboarding)."
    )
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Company created"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid employerId")
    })
    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> createCompany(
            @Valid @RequestBody CompanyDTO companyDTO,
            @Parameter(description = "Employer UUID that owns the new company", required = true)
            @Valid @RequestParam("employerId") UUID employerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iCompanyService.createCompany(companyDTO, employerId));
    }

    @Operation(
            summary = "Get current company",
            description = "Returns the active company for the authenticated employer context."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company retrieved"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping(value = "/details")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> getCompany(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.getCompany());
    }

    @Operation(
            summary = "List companies",
            description = "Paginated list of companies for the authenticated employer."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of companies"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping(value = "/companies")
    public ResponseEntity<DefaultApiResponse<List<CompanyDTO>>> getCompaniesByEmployerId(
            @Parameter(description = "Zero-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.getCompaniesByEmployerId(page, size));
    }

    @Operation(
            summary = "Update company",
            description = "Update the current company profile."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PutMapping(value = "/update")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> updateCompany(
            @Valid @RequestBody CompanyUpdateDTO companyUpdateDTO){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.updateCompany(companyUpdateDTO));
    }

    @Operation(
            summary = "Delete company",
            description = "Deletes the current company per application rules."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Company deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @DeleteMapping(value = "/delete")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> deleteCompany(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.deleteCompany());
    }
}
