package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.WebhookResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.util.Objects;

@Slf4j @Service @RequiredArgsConstructor
public class KoraPayWebhook {

    @Value("${kora-secret}")
    private String SECRET_KEY;

    public ResponseEntity<String> verifyWebHook(WebhookResponseDTO payload, String signature) throws JsonProcessingException {
        log.info("Webhook payload: {}", payload);
        log.info("Received signature: {}", signature);

        // Verifying the webhook's authenticity using the WebhookVerifier provided by Kora
        String secretKey = SECRET_KEY;

        ObjectMapper objectMapper = new ObjectMapper();
        String payloadJson = objectMapper.writeValueAsString(payload);
        boolean isValid = WebhookVerifier.verifySignature(payloadJson, signature, secretKey);

        if (isValid) {
//            processEvent(payload);
            log.info("Webhook verified successfully");
            return ResponseEntity.status(HttpStatus.OK).body("Webhook verified");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
    }
}
