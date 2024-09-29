package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Method to find all payments by virtual account's ID and order by transactionDateTime desc
    Page<Payment> findAllByAccount_AccountIdOrderByTransactionDateTimeDesc(UUID accountId, Pageable pageable);

    List<Payment> findAllByAccount_AccountId(UUID accountId);
}
