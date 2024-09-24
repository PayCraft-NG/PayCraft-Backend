package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
    @Query("SELECT p FROM Payroll p WHERE p.automatic IS true")
    List<Payroll> findAddWhereAutomaticIsTrue();
}
