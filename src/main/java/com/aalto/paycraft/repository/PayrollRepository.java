package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PayrollRepository extends JpaRepository<Payroll, UUID> {
}
