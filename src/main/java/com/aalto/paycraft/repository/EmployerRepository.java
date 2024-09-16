package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, UUID> {
    Optional<Employer> findByEmailAddress(String emailAddress);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmailAddress(String emailAddress);
    boolean existsByBvn(String bvn);
}
