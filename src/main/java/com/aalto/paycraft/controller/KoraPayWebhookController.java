package com.aalto.paycraft.controller;

import com.aalto.paycraft.dto.WebhookResponseDTO;
import com.aalto.paycraft.service.KoraPayWebhook;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;


@Tag(
        name = "KoraPayWebhook Controller",
        description = "ENDPOINT to handle Notifications from KoraPay - Server Side Only"
)
@RestController @Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class KoraPayWebhookController {

    // Dependency on KoraPayWebhook service, which handles the business logic for processing webhooks.
    private final KoraPayWebhook webhookService;

    @Operation(summary = "Handle Webhook Response for Bank Transfer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Webhook Verified"),
            @ApiResponse(responseCode = "400", description = "Invalid payload")
    })
    @PostMapping("/webhook")
    @Async(value = "taskExecutor")
    public CompletableFuture<ResponseEntity<String>> handleKoraPayWebHook(@RequestBody WebhookResponseDTO<?> payload, @RequestHeader("X-Korapay-Signature") String signature) {
        // Method receives a webhook payload and signature header for validation.
        try {
            // Logging the incoming payload for debugging purposes.
            log.info("Received KoraPay Webhook String: {}", payload.toString());

            // Calls the webhookService to verify the incoming webhook and returns a 200 response if successful.
            return CompletableFuture.completedFuture(ResponseEntity.status(200).body(webhookService.verifyWebHook(payload, signature)));
        } catch (JsonProcessingException e) {
            // Handles any JSON processing exceptions (e.g., if the payload cannot be parsed).
            log.error("Error processing KoraPay webhook", e);

            // Returns a 400 Bad Request response if the payload is invalid or cannot be processed.
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload"));
        } catch (Exception e) {
            // Catches any other unexpected exceptions and logs the error.
            log.error("Unexpected error processing KoraPay webhook", e);

            // Returns a 500 Internal Server Error if any other exception occurs during processing.
            return CompletableFuture.completedFuture(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook handling failed"));
        }
    }

    @GetMapping("/home")
    public ResponseEntity<String> getHome() {
        return ResponseEntity.status(HttpStatus.OK).body("Home Page");
    }
}
