package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, UUID> {

    // Find only active employers (where deleted is false)
    @Query("SELECT e FROM Employer e WHERE e.emailAddress = :emailAddress AND e.deleted = false")
    Optional<Employer> findByEmailAddress(String emailAddress);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employer e WHERE e.phoneNumber = :phoneNumber AND e.deleted = false")
    boolean existsByPhoneNumber(String phoneNumber);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employer e WHERE e.emailAddress = :emailAddress AND e.deleted = false")
    boolean existsByEmailAddress(String emailAddress);

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN TRUE ELSE FALSE END FROM Employer e WHERE e.bvn = :bvn AND e.deleted = false")
    boolean existsByBvn(String bvn);
}
