package com.aalto.paycraft.repository;

import com.aalto.paycraft.entity.WebhookData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WebhookDataRepository extends JpaRepository<WebhookData, Long> {
    // Get WebHook Data by the Reference Number
    Optional<WebhookData> findByReference(String reference);
}
