package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.CompanyUpdateDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.service.ICompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
        name = "Company Controller",
        description = "CRUD REST APIs in payroll to CREATE, READ, UPDATE, and DELETE Company details"
)
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/company", produces = MediaType.APPLICATION_JSON_VALUE)
public class CompanyController {
    private final ICompanyService iCompanyService;

    @Operation(
            summary = "Create Company REST API",
            description = "REST API to create a new Company"
    )
    @ApiResponse(
            responseCode = "201",
            description = "HTTP Status CREATED"
    )
    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> createCompany(
            @Valid @RequestBody CompanyDTO companyDTO,
            @Valid @RequestParam("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iCompanyService.createCompany(companyDTO, employerId));
    }

    @Operation(
            summary = "Get Company Details REST API",
            description = "REST API to get company details by companyId"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping(value = "/{companyId}")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> getCompany(
            @Valid @PathVariable("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.getCompany(companyId));
    }

    @Operation(
            summary = "Get List of Companies REST API",
            description = "REST API to get a list of companies by employerId"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @GetMapping(value = "/companies")
    public ResponseEntity<DefaultApiResponse<List<CompanyDTO>>> getCompaniesByEmployerId(
            @Valid @RequestParam("employerId") UUID employerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.getCompaniesByEmployerId(employerId, page, size));
    }

    @Operation(
            summary = "Update Company Details REST API",
            description = "REST API to update company details by companyId"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @PutMapping(value = "/update/{companyId}")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> updateCompany(
            @Valid @RequestBody CompanyUpdateDTO companyUpdateDTO,
            @Valid @PathVariable("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.updateCompany(companyUpdateDTO, companyId));
    }

    @Operation(
            summary = "Delete Company REST API",
            description = "REST API to delete company by companyId"
    )
    @ApiResponse(
            responseCode = "200",
            description = "HTTP Status OK"
    )
    @DeleteMapping(value = "/delete/{companyId}")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> deleteCompany(
            @Valid @PathVariable("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.deleteCompany(companyId));
    }
}
