package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    boolean existsByEmailAddressAndPhoneNumberAndCompany_CompanyIdAndDeletedIsFalse(String emailAddress, String phoneNumber, UUID companyProfileId);
}
