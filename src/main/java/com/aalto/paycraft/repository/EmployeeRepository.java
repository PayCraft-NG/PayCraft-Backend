package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    // Find only active employees (where deleted is false)
    @Query("SELECT e FROM Employee e WHERE e.employeeId = :employeeId AND e.deleted = false")
    Optional<Employee> findByEmployeeId(UUID employeeId);

    // Check if Employee exists by Email Address, Phone Number, CompanyID and make sure the account has not been deleted
    boolean existsByEmailAddressAndPhoneNumberAndCompany_CompanyIdAndDeletedIsFalse(String emailAddress, String phoneNumber, UUID companyProfileId);
}
