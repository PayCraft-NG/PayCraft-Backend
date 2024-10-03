package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Company;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByCompanyNameAndEmployer_EmployerId(String companyName, UUID employerId);
    List<Company> findAllByEmployer_EmployerId(UUID employerId, Pageable pageable);
    boolean existsByCompanyPhoneNumber(String phoneNumber);
    Optional<Company> findByCompanyPhoneNumber(String phoneNumber);
    boolean existsByCompanyEmailAddress(String companyEmailAddress);
}
