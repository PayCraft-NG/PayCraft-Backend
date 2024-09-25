package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.WebhookResponseDTO;
import com.aalto.paycraft.service.KoraPayWebhook;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class KoraPayWebhookController {

    private final KoraPayWebhook webhookService;

    private static final Logger logger = LoggerFactory.getLogger(KoraPayWebhookController.class);

    @PostMapping("/webhook")
    public ResponseEntity<String> handleKoraPayWebHook(
            @RequestBody WebhookResponseDTO payload,
            @RequestHeader("x-kora-signature") String signature) {

        try {
            logger.info("Received KoraPay webhook: {}", payload);
            return webhookService.verifyWebHook(payload, signature);
        } catch (JsonProcessingException e) {
            logger.error("Error processing KoraPay webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        } catch (Exception e) {
            logger.error("Unexpected error processing KoraPay webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook handling failed");
        }
    }
}
