package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.WebhookResponseDTO;
import com.aalto.paycraft.entity.WebhookData;
import com.aalto.paycraft.repository.WebhookDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

@Slf4j @Service @RequiredArgsConstructor
public class KoraPayWebhook {

    @Value("${kora-secret}")
    private String SECRET_KEY;

    private final WebhookDataRepository webhookDataRepository;

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    // Convert bytes to hex format
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String (hexChars);
    }

    // Method to verify the signature
    public boolean verifySignature(String webhookData, String signature, String korapaySecretKey) {
        try {
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(korapaySecretKey.getBytes("UTF-8"), "HmacSHA256");
            sha256HMAC.init(secretKey);

            String generatedSignature = bytesToHex(sha256HMAC.doFinal(webhookData.getBytes("UTF-8")));

            log.info("Generated Signature {}", generatedSignature);
            log.info("Received signature: {}", signature);


            // Compare the generated HMAC hex with the signature from Korapay
            return generatedSignature.equals(signature);

        } catch (Exception e) {
            log.error("An error occurred while verifying the signature", e);
            return false;
        }
    }

    public String verifyWebHook(WebhookResponseDTO payload, String signature) throws JsonProcessingException {
        // Verifying the webhook's authenticity using the WebhookVerifier provided by Kora
        String secretKey = SECRET_KEY;
        log.info("Secret key: {}", secretKey);

        ObjectMapper objectMapper = new ObjectMapper();
        String payloadJson = objectMapper.writeValueAsString(payload.getData());

        log.info("Received Payload: {}", payloadJson);
        boolean isValid = verifySignature(payloadJson, signature, secretKey);

        if (isValid) {
            log.info("Webhook verified successfully");
            WebhookData webhookData = WebhookData.builder()
                    .event(payload.getEvent())
                    .reference(payload.getData().getReference())
                    .currency(payload.getData().getCurrency())
                    .amount(payload.getData().getAmount())
                    .fee(payload.getData().getFee())
                    .status(payload.getData().getStatus())
                    .build();
            webhookDataRepository.save(webhookData);
            return "Webhook verified";
        } else {
            return "Invalid signature";
        }
    }
}
