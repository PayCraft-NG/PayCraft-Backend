package com.aalto.paycraft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.Map;

@Slf4j @Service @RequiredArgsConstructor
public class KoraPayWebhook {

    @Value("${kora-secret}")
    private String SECRET_KEY;

    public ResponseEntity<String> verifyWebHook(Map<String, String> payload, String signature) {
        log.info("Webhook payload: {}", payload);
        log.info("Received signature: {}", signature);

        // Verifying the webhook's authenticity using the WebhookVerifier provided by Kora
        String secretKey = SECRET_KEY;
        boolean isValid = WebhookVerifier.verifySignature(payload.toString(), signature, secretKey);

        if (isValid) {
//            processEvent(payload);
            return ResponseEntity.status(HttpStatus.OK).body("Webhook verified");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }
    }
}
