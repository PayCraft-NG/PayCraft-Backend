package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findAllByAccount_AccountId(UUID accountId);
}
