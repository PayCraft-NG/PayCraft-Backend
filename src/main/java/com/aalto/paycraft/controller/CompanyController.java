package com.aalto.paycraft.controller;


import com.aalto.paycraft.dto.CompanyDTO;
import com.aalto.paycraft.dto.DefaultApiResponse;
import com.aalto.paycraft.service.ICompanyService;
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
@RequestMapping(value = "/api/v1/company", produces = MediaType.APPLICATION_JSON_VALUE)
public class CompanyController {
    private final ICompanyService iCompanyService;

    @PostMapping(value = "/create")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> createCompany(
            @Valid @RequestBody CompanyDTO companyDTO,
            @Valid @RequestParam("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.createCompany(companyDTO, employerId));
    }

    @GetMapping(value = "/{companyId}")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> getCompany(
            @Valid @PathVariable("companyId")UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.getCompany(companyId));
    }

    @GetMapping(value = "/companies")
    public ResponseEntity<DefaultApiResponse<List<CompanyDTO>>> getCompaniesByEmployerId(
            @Valid @RequestParam("employerId") UUID employerId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.getCompaniesByEmployerId(employerId));
    }

    @PutMapping(value = "/update/{companyId}")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> updateCompany(
            @Valid @PathVariable("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.updateCompany(companyId));
    }

    @DeleteMapping(value = "/delete/{companyId}")
    public ResponseEntity<DefaultApiResponse<CompanyDTO>> deleteCompany(
            @Valid @PathVariable("companyId") UUID companyId){
        return ResponseEntity.status(HttpStatus.OK)
                .body(iCompanyService.deleteCompany(companyId));
    }
}
