package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Company;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByCompanyNameAndEmployer_EmployerId(String companyName, UUID employerId);
    List<Company> findAllByEmployer_EmployerId(UUID employerId, Pageable pageable);
    boolean existsByCompanyPhoneNumber(String phoneNumber);
    boolean existsByCompanyEmailAddress(String companyEmailAddress);
}
