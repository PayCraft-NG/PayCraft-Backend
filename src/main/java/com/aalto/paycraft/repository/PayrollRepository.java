package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    @Query("SELECT p FROM Payroll p WHERE p.automatic IS true")
    List<Payroll> findAddWhereAutomaticIsTrue();

    // Custom JPQL Query to find all Payroll by company ID
    @Query("SELECT p FROM Payroll p WHERE p.company.companyId = :companyId")
    List<Payroll> findAllByCompanyId(UUID companyId);

    Optional<Payroll> findByPayrollId(UUID payrollId);
}
