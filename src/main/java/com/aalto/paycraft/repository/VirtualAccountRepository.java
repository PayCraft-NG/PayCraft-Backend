package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.VirtualAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VirtualAccountRepository extends JpaRepository<VirtualAccount, UUID> {
    Optional<VirtualAccount> findByEmployer_EmployerId(UUID employerId);
}
