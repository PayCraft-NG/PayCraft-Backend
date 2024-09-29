package com.aalto.paycraft.service;

import com.aalto.paycraft.dto.WebhookResponseDTO;
import com.aalto.paycraft.dto.WebhookResponseData;
import com.aalto.paycraft.dto.WebhookResponseDataVba;
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

        return new String(hexChars);
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

    public String verifyWebHook(WebhookResponseDTO<?> webhookResponseDTO, String signature) throws JsonProcessingException {
        // Secret key should be managed securely, avoid logging it
        String secretKey = SECRET_KEY;
        log.debug("Verifying webhook with provided signature");

        if (webhookResponseDTO == null || signature == null) {
            log.error("Invalid webhook or signature");
            return "Invalid request";
        }

        Object data = webhookResponseDTO.getData();
        if (data == null) {
            log.error("Webhook payload data is null");
            return "Invalid payload";
        }

        // Handle both WebhookResponseData and WebhookResponseDataVba in a unified way
        String payloadJson = convertPayloadToJson(data);
        if (payloadJson == null) {
            log.error("Error converting payload to JSON");
            return "Invalid payload";
        }

        log.info("Received payload: {}", payloadJson);
        boolean isValid = verifySignature(payloadJson, signature, secretKey);

        if (isValid) {
            log.info("Webhook verified successfully");
            WebhookData webhookData = buildWebhookData(webhookResponseDTO, data);
            webhookDataRepository.save(webhookData);
            return "Webhook verified";
        } else {
            log.warn("Invalid signature detected");
            return "Invalid signature";
        }
    }

    private String convertPayloadToJson(Object data) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.error("Error processing JSON for payload: {}", e.getMessage());
            return null;
        }
    }

    private WebhookData buildWebhookData(WebhookResponseDTO<?> webhookResponseDTO, Object data) {
        // Parse the 'data' object dynamically as a map (assuming it can be serialized from JSON)
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) data;

            // Extracting values directly from the map
            String reference = (String) dataMap.getOrDefault("reference", null);
            String currency = (String) dataMap.getOrDefault("currency", null);
            Double amount = dataMap.get("amount") != null ? Double.valueOf(dataMap.get("amount").toString()) : null;
            Double fee = dataMap.get("fee") != null ? Double.valueOf(dataMap.get("fee").toString()) : null;
            String status = (String) dataMap.getOrDefault("status", null);

            // Build and return the WebhookData object using extracted values
            return WebhookData.builder()
                    .event(webhookResponseDTO.getEvent())
                    .reference(reference)
                    .currency(currency)
                    .amount(BigDecimal.valueOf(amount))
                    .fee(BigDecimal.valueOf(fee))
                    .status(status)
                    .build();
        }

        // If data is not in a format we can process
        throw new IllegalArgumentException("Unsupported payload data type");
    }
}
