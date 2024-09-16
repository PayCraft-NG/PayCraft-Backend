package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployerProfileRepository extends JpaRepository<Employer, UUID> {
    Optional<Employer> findByPhoneNumber(String phoneNumber);
    Optional<Employer> findByEmailAddress(String emailAddress);
}