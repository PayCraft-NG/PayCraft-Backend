package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    // Find only active employees (where deleted is false)
    @Query("SELECT e FROM Employee e WHERE e.employeeId = :employeeId AND e.deleted = false")
    Optional<Employee> findByEmployeeId(UUID employeeId);

    @Query("SELECT e FROM Employee e WHERE e.deleted = false AND e.company.companyId = :companyId")
    List<Employee> findAllByDeletedFalseAndCompanyId(UUID companyId);

    // Check if Employee exists by Email Address, Phone Number, CompanyID and make sure the account has not been deleted
    boolean existsByEmailAddressAndPhoneNumberAndCompany_CompanyIdAndDeletedIsFalse(String emailAddress, String phoneNumber, UUID companyProfileId);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employee e WHERE e.phoneNumber = :phoneNumber AND e.deleted = false")
    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employee e WHERE e.emailAddress = :emailAddress AND e.deleted = false")
    boolean existsByEmailAddress(String emailAddress);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employee e WHERE e.bvn = :bvn AND e.deleted = false")
    boolean existsByBvn(String bvn);
}
